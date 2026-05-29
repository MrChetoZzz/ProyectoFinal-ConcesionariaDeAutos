package com.concesionaria.app.controller;

import com.concesionaria.app.service.BitacoraNoSqlService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final JdbcTemplate jdbcTemplate;
    private final BitacoraNoSqlService bitacoraNoSqlService;

    public ReporteController(JdbcTemplate jdbcTemplate, BitacoraNoSqlService bitacoraNoSqlService) {
        this.jdbcTemplate = jdbcTemplate;
        this.bitacoraNoSqlService = bitacoraNoSqlService;
    }

    @GetMapping("/vehiculos-vendidos")
    public List<VehiculoVendidoDto> reporteVehiculosVendidos() {
        return jdbcTemplate.query("""
            SELECT
                v.Marca,
                v.Modelo,
                COUNT(*) AS Unidades,
                SUM(venta.CostoTotal) AS Total
            FROM Venta venta
            INNER JOIN Vehiculo v ON v.IdVehiculo = venta.IdVehiculo
            WHERE venta.IdVentaEstado = 2
            GROUP BY v.Marca, v.Modelo
            ORDER BY COUNT(*) DESC, SUM(venta.CostoTotal) DESC
            """, (rs, rowNum) -> new VehiculoVendidoDto(
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("Unidades"),
                rs.getBigDecimal("Total")
            ));
    }

    @PostMapping(value = "/vehiculos-vendidos/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarVehiculosVendidosPdf(
        @RequestBody ReporteVehiculosVendidosPdfRequest request,
        HttpServletRequest servletRequest
    ) {
        if (request == null || request.filas() == null || request.filas().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecciona al menos una fila para descargar el PDF.");
        }

        var pdf = generarPdfVehiculosVendidos(request.filas());
        bitacoraNoSqlService.registrarDescargaReporte(
            "vehiculos-vendidos",
            "VehiculosVendidos",
            "PDF",
            Map.of("filasSeleccionadas", request.filas().size()),
            servletRequest
        );

        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_PDF)
            .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                .filename("reporte-vehiculos-vendidos.pdf")
                .build()
                .toString())
            .body(pdf);
    }

    private static byte[] generarPdfVehiculosVendidos(List<VehiculoVendidoDto> filas) {
        var content = new StringBuilder();
        text(content, 50, 790, 18, "Reporte de vehiculos vendidos");
        text(content, 50, 765, 10, "Fecha de descarga: " + LocalDate.now());
        text(content, 50, 735, 11, "Marca");
        text(content, 180, 735, 11, "Modelo");
        text(content, 320, 735, 11, "Unidades");
        text(content, 410, 735, 11, "Total");
        line(content, 50, 728, 545, 728);

        var y = 710;
        var totalUnidades = 0;
        var totalImporte = BigDecimal.ZERO;
        for (var fila : filas) {
            if (y < 90) {
                text(content, 50, y, 10, "El reporte continua con mas filas seleccionadas.");
                break;
            }
            var unidades = fila.unidades() == null ? 0 : fila.unidades();
            var total = fila.total() == null ? BigDecimal.ZERO : fila.total();
            text(content, 50, y, 10, safe(fila.marca(), 20));
            text(content, 180, y, 10, safe(fila.modelo(), 22));
            text(content, 335, y, 10, String.valueOf(unidades));
            text(content, 410, y, 10, formatMoney(total));
            totalUnidades += unidades;
            totalImporte = totalImporte.add(total);
            y -= 20;
        }

        line(content, 50, y + 8, 545, y + 8);
        text(content, 50, y - 10, 11, "Total seleccionado");
        text(content, 335, y - 10, 11, String.valueOf(totalUnidades));
        text(content, 410, y - 10, 11, formatMoney(totalImporte));

        return buildSinglePagePdf(content.toString());
    }

    private static byte[] buildSinglePagePdf(String content) {
        var stream = content.getBytes(StandardCharsets.ISO_8859_1);
        var objects = new ArrayList<byte[]>();
        objects.add("1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        objects.add("2 0 obj\n<< /Type /Pages /Kids [3 0 R] /Count 1 >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        objects.add("3 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 612 792] /Resources << /Font << /F1 4 0 R >> >> /Contents 5 0 R >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        objects.add("4 0 obj\n<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>\nendobj\n".getBytes(StandardCharsets.US_ASCII));
        objects.add(("5 0 obj\n<< /Length " + stream.length + " >>\nstream\n" + content + "\nendstream\nendobj\n").getBytes(StandardCharsets.ISO_8859_1));

        var pdf = new StringBuilder("%PDF-1.4\n");
        var offsets = new ArrayList<Integer>();
        for (var object : objects) {
            offsets.add(pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length);
            pdf.append(new String(object, StandardCharsets.ISO_8859_1));
        }
        var xrefOffset = pdf.toString().getBytes(StandardCharsets.ISO_8859_1).length;
        pdf.append("xref\n0 ").append(objects.size() + 1).append("\n");
        pdf.append("0000000000 65535 f \n");
        for (var offset : offsets) {
            pdf.append(String.format(Locale.US, "%010d 00000 n \n", offset));
        }
        pdf.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\n");
        pdf.append("startxref\n").append(xrefOffset).append("\n%%EOF");
        return pdf.toString().getBytes(StandardCharsets.ISO_8859_1);
    }

    private static void text(StringBuilder content, int x, int y, int size, String value) {
        content.append("BT /F1 ")
            .append(size)
            .append(" Tf ")
            .append(x)
            .append(' ')
            .append(y)
            .append(" Td (")
            .append(escapePdf(value))
            .append(") Tj ET\n");
    }

    private static void line(StringBuilder content, int x1, int y1, int x2, int y2) {
        content.append(x1).append(' ')
            .append(y1).append(" m ")
            .append(x2).append(' ')
            .append(y2).append(" l S\n");
    }

    private static String escapePdf(String value) {
        var normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return normalized
            .replace("\\", "\\\\")
            .replace("(", "\\(")
            .replace(")", "\\)");
    }

    private static String safe(String value, int maxLength) {
        var normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD)
            .replaceAll("\\p{M}", "");
        return normalized.length() <= maxLength ? normalized : normalized.substring(0, maxLength - 1) + ".";
    }

    private static String formatMoney(BigDecimal value) {
        return String.format(Locale.US, "$%,.2f", value);
    }

    public record VehiculoVendidoDto(
        String marca,
        String modelo,
        Integer unidades,
        BigDecimal total
    ) {
    }

    public record ReporteVehiculosVendidosPdfRequest(
        List<VehiculoVendidoDto> filas
    ) {
    }
}
