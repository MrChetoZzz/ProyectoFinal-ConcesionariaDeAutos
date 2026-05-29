package com.concesionaria.app.service;

import jakarta.servlet.http.HttpServletRequest;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class BitacoraNoSqlService {

    private final MongoTemplate mongoTemplate;

    public BitacoraNoSqlService(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    public void registrarDescargaReporte(
        String idReporte,
        String nombreReporte,
        String formato,
        Map<String, Object> filtrosUsados,
        HttpServletRequest request
    ) {
        var detalles = new LinkedHashMap<String, Object>();
        detalles.put("idReporte", idReporte);
        detalles.put("nombreReporte", nombreReporte);
        detalles.put("formato", formato);
        detalles.put("filtrosUsados", filtrosUsados);

        var documento = new LinkedHashMap<String, Object>();
        documento.put("fechaHora", Instant.now());
        documento.put("idUsuario", 0);
        documento.put("modulo", "REPORTES");
        documento.put("ip", obtenerIp(request));
        documento.put("correo", null);
        documento.put("detalles", detalles);

        insertarSinRomperFlujo("bitacora_descarga_reportes", documento);
    }

    public void registrarError(String mensaje, Integer codigoError, String stackTrace, HttpServletRequest request) {
        var contexto = new LinkedHashMap<String, Object>();
        contexto.put("url", request == null ? null : request.getRequestURI());
        contexto.put("metodo", request == null ? null : request.getMethod());
        contexto.put("ip", obtenerIp(request));
        contexto.put("navegador", request == null ? null : request.getHeader("User-Agent"));

        var documento = new LinkedHashMap<String, Object>();
        documento.put("fechaHora", Instant.now());
        documento.put("nivel", "ERROR");
        documento.put("mensaje", mensaje);
        documento.put("codigoError", codigoError);
        documento.put("stackTrace", stackTrace);
        documento.put("contexto", contexto);

        insertarSinRomperFlujo("bitacora_logs", documento);
    }

    public List<DescargaReporteDto> listarDescargasReportes() {
        return mongoTemplate.findAll(Document.class, "bitacora_descarga_reportes")
            .stream()
            .sorted((left, right) -> {
                var leftDate = left.getDate("fechaHora");
                var rightDate = right.getDate("fechaHora");
                if (leftDate == null && rightDate == null) return 0;
                if (leftDate == null) return 1;
                if (rightDate == null) return -1;
                return rightDate.compareTo(leftDate);
            })
            .map(BitacoraNoSqlService::mapDescargaReporte)
            .toList();
    }

    private void insertarSinRomperFlujo(String coleccion, Map<String, Object> documento) {
        try {
            mongoTemplate.insert(documento, coleccion);
        } catch (Exception ignored) {
            // La bitacora no debe impedir operaciones del sistema principal.
        }
    }

    private static String obtenerIp(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        var forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    @SuppressWarnings("unchecked")
    private static DescargaReporteDto mapDescargaReporte(Document document) {
        var detalles = (Map<String, Object>) document.getOrDefault("detalles", Map.of());
        var filtros = (Map<String, Object>) detalles.getOrDefault("filtrosUsados", Map.of());
        var fechaHora = document.getDate("fechaHora");
        return new DescargaReporteDto(
            String.valueOf(document.getObjectId("_id")),
            fechaHora == null ? null : fechaHora.toInstant(),
            getInteger(document.get("idUsuario")),
            stringOrNull(document.get("modulo")),
            stringOrNull(document.get("ip")),
            stringOrNull(document.get("correo")),
            stringOrNull(detalles.get("idReporte")),
            stringOrNull(detalles.get("nombreReporte")),
            stringOrNull(detalles.get("formato")),
            filtros
        );
    }

    private static Integer getInteger(Object value) {
        if (value instanceof Number number) {
            return number.intValue();
        }
        return null;
    }

    private static String stringOrNull(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    public record DescargaReporteDto(
        String id,
        Instant fechaHora,
        Integer idUsuario,
        String modulo,
        String ip,
        String correo,
        String idReporte,
        String nombreReporte,
        String formato,
        Map<String, Object> filtrosUsados
    ) {
    }
}
