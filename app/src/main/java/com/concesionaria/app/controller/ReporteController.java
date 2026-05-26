package com.concesionaria.app.controller;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final JdbcTemplate jdbcTemplate;

    public ReporteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
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

    public record VehiculoVendidoDto(
        String marca,
        String modelo,
        Integer unidades,
        BigDecimal total
    ) {
    }
}
