package com.concesionaria.app.controller;

import com.concesionaria.app.service.BitacoraNoSqlService;
import com.concesionaria.app.service.BitacoraNoSqlService.DescargaReporteDto;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bitacora")
public class BitacoraController {

    private final BitacoraNoSqlService bitacoraNoSqlService;

    public BitacoraController(BitacoraNoSqlService bitacoraNoSqlService) {
        this.bitacoraNoSqlService = bitacoraNoSqlService;
    }

    @GetMapping("/reportes-descargados")
    public List<DescargaReporteDto> listarReportesDescargados() {
        return bitacoraNoSqlService.listarDescargasReportes();
    }

    @PostMapping("/reportes-descargados")
    public Mensaje registrarReporteDescargado(@RequestBody RegistroReporteRequest request, HttpServletRequest servletRequest) {
        bitacoraNoSqlService.registrarDescargaReporte(
            request.idReporte(),
            request.nombreReporte(),
            request.formato(),
            request.filtrosUsados() == null ? Map.of() : request.filtrosUsados(),
            servletRequest
        );
        return new Mensaje("Bitacora registrada correctamente.");
    }

    public record RegistroReporteRequest(
        String idReporte,
        String nombreReporte,
        String formato,
        Map<String, Object> filtrosUsados
    ) {
    }

    public record Mensaje(String mensaje) {
    }
}
