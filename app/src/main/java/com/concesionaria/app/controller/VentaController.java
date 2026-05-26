package com.concesionaria.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/ventas")
public class VentaController {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbc;

    public VentaController(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbc = namedJdbc;
    }

    @GetMapping
    public List<VentaDto> listarVentas(@RequestParam(required = false, defaultValue = "Fecha-Descendente") String sort) {
        return jdbcTemplate.query("""
            SELECT
                venta.IdVenta,
                venta.FechaIngreso,
                venta.CostoTotal,
                ve.DescripcionEs AS Estado,
                v.IdVehiculo,
                v.Marca,
                v.Modelo,
                c.IdCliente,
                p.Nombre AS Cliente,
                u.IdUsuario
            FROM Venta venta
            INNER JOIN VentaEstado ve ON ve.IdVentaEstado = venta.IdVentaEstado
            INNER JOIN Vehiculo v ON v.IdVehiculo = venta.IdVehiculo
            INNER JOIN Cliente c ON c.IdCliente = venta.IdCliente
            INNER JOIN Persona p ON p.IdPersona = c.IdPersona
            INNER JOIN Usuario u ON u.IdUsuario = venta.IdUsuario
            """ + orderBy(sort), (rs, rowNum) -> new VentaDto(
                rs.getInt("IdVenta"),
                rs.getObject("FechaIngreso", LocalDateTime.class),
                rs.getBigDecimal("CostoTotal"),
                rs.getString("Estado"),
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("IdCliente"),
                rs.getString("Cliente"),
                rs.getInt("IdUsuario")
            ));
    }

    @GetMapping("/tipos-pago")
    public List<TipoPagoDto> listarTiposPago() {
        return jdbcTemplate.query("""
            SELECT IdTipoPago, TipoPago
            FROM TipoPago
            ORDER BY IdTipoPago
            """, (rs, rowNum) -> new TipoPagoDto(
                rs.getInt("IdTipoPago"),
                rs.getString("TipoPago")
            ));
    }

    @PostMapping
    @Transactional
    public VentaDto registrarVenta(@RequestBody VentaRequest request) {
        if (request.idCliente() == null || request.idVehiculo() == null || request.idTipoPago() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente, vehiculo y tipo de pago son obligatorios.");
        }

        var costoTotal = request.costoTotal() != null ? request.costoTotal() : obtenerCostoVehiculo(request.idVehiculo());
        var monto = request.monto() != null ? request.monto() : costoTotal;
        var idUsuario = request.idUsuario() != null ? request.idUsuario() : obtenerPrimerUsuarioActivo();
        var fecha = request.fecha() != null ? request.fecha().atStartOfDay() : LocalDateTime.now();
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                INSERT INTO Venta (IdUsuario, IdCliente, IdVehiculo, IdVentaEstado, CostoTotal, FechaIngreso)
                VALUES (?, ?, ?, 2, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, idUsuario);
            ps.setInt(2, request.idCliente());
            ps.setInt(3, request.idVehiculo());
            ps.setBigDecimal(4, costoTotal);
            ps.setObject(5, fecha);
            return ps;
        }, keyHolder);

        var idVenta = Objects.requireNonNull(keyHolder.getKey()).intValue();
        jdbcTemplate.update("""
            INSERT INTO MovimientoFinanciero (IdVenta, IdTipoPago, Monto, FechaIngreso)
            VALUES (?, ?, ?, ?)
            """, idVenta, request.idTipoPago(), monto, fecha);

        return obtenerVenta(idVenta);
    }

    private VentaDto obtenerVenta(int idVenta) {
        return namedJdbc.query("""
            SELECT
                venta.IdVenta,
                venta.FechaIngreso,
                venta.CostoTotal,
                ve.DescripcionEs AS Estado,
                v.IdVehiculo,
                v.Marca,
                v.Modelo,
                c.IdCliente,
                p.Nombre AS Cliente,
                u.IdUsuario
            FROM Venta venta
            INNER JOIN VentaEstado ve ON ve.IdVentaEstado = venta.IdVentaEstado
            INNER JOIN Vehiculo v ON v.IdVehiculo = venta.IdVehiculo
            INNER JOIN Cliente c ON c.IdCliente = venta.IdCliente
            INNER JOIN Persona p ON p.IdPersona = c.IdPersona
            INNER JOIN Usuario u ON u.IdUsuario = venta.IdUsuario
            WHERE venta.IdVenta = :idVenta
            """, new MapSqlParameterSource("idVenta", idVenta), (rs, rowNum) -> new VentaDto(
                rs.getInt("IdVenta"),
                rs.getObject("FechaIngreso", LocalDateTime.class),
                rs.getBigDecimal("CostoTotal"),
                rs.getString("Estado"),
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("IdCliente"),
                rs.getString("Cliente"),
                rs.getInt("IdUsuario")
            )).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Venta no encontrada."));
    }

    private BigDecimal obtenerCostoVehiculo(int idVehiculo) {
        var costo = jdbcTemplate.queryForObject(
            "SELECT Costo FROM Vehiculo WHERE IdVehiculo = ?",
            BigDecimal.class,
            idVehiculo
        );
        if (costo == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El vehiculo no tiene costo registrado.");
        }
        return costo;
    }

    private int obtenerPrimerUsuarioActivo() {
        return jdbcTemplate.query("""
            SELECT TOP 1 IdUsuario
            FROM Usuario
            WHERE EstaActivo = 1
            ORDER BY CASE WHEN IdRolUsuario = 1 THEN 0 ELSE 1 END, IdUsuario
            """, rs -> {
            if (!rs.next()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay usuarios activos para registrar la venta.");
            }
            return rs.getInt("IdUsuario");
        });
    }

    private static String orderBy(String sort) {
        return " ORDER BY " + Map.ofEntries(
            Map.entry("Folio-Ascendente", "venta.IdVenta ASC"),
            Map.entry("Folio-Descendente", "venta.IdVenta DESC"),
            Map.entry("Nombre-Ascendente", "p.Nombre ASC"),
            Map.entry("Nombre-Descendente", "p.Nombre DESC"),
            Map.entry("Marca-Ascendente", "v.Marca ASC"),
            Map.entry("Marca-Descendente", "v.Marca DESC"),
            Map.entry("Modelo-Ascendente", "v.Modelo ASC"),
            Map.entry("Modelo-Descendente", "v.Modelo DESC"),
            Map.entry("Precio-Ascendente", "venta.CostoTotal ASC"),
            Map.entry("Precio-Descendente", "venta.CostoTotal DESC"),
            Map.entry("Fecha-Ascendente", "venta.FechaIngreso ASC"),
            Map.entry("Fecha-Descendente", "venta.FechaIngreso DESC")
        ).getOrDefault(sort, "venta.FechaIngreso DESC");
    }

    public record VentaDto(
        Integer id,
        LocalDateTime fecha,
        BigDecimal costoTotal,
        String estado,
        Integer idVehiculo,
        String marca,
        String modelo,
        Integer idCliente,
        String cliente,
        Integer idUsuario
    ) {
    }

    public record TipoPagoDto(Integer id, String tipoPago) {
    }

    public record VentaRequest(
        Integer idCliente,
        Integer idVehiculo,
        Integer idTipoPago,
        BigDecimal monto,
        BigDecimal costoTotal,
        LocalDate fecha,
        Integer idUsuario
    ) {
    }
}
