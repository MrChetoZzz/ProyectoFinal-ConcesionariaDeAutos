package com.concesionaria.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
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
import java.util.Objects;

@RestController
@RequestMapping("/api/mecanica")
public class MecanicaController {

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedJdbc;

    public MecanicaController(JdbcTemplate jdbcTemplate, NamedParameterJdbcTemplate namedJdbc) {
        this.jdbcTemplate = jdbcTemplate;
        this.namedJdbc = namedJdbc;
    }

    @GetMapping("/mecanicos")
    public List<MecanicoDto> listarMecanicos() {
        return jdbcTemplate.query("""
            SELECT
                m.IdMecanico,
                p.Nombre,
                e.Especializacion
            FROM Mecanico m
            INNER JOIN Persona p ON p.IdPersona = m.IdPersona
            INNER JOIN EspecializacionMecanico e ON e.IdEspecializacionMecanico = m.IdEspecializacionMecanico
            WHERE m.EstaActivo = 1
            ORDER BY p.Nombre
            """, (rs, rowNum) -> new MecanicoDto(
                rs.getInt("IdMecanico"),
                rs.getString("Nombre"),
                rs.getString("Especializacion")
            ));
    }

    @GetMapping("/clientes/{idCliente}/vehiculos")
    public List<VehiculoClienteDto> listarVehiculosComprados(@PathVariable int idCliente) {
        return jdbcTemplate.query("""
            SELECT DISTINCT
                v.IdVehiculo,
                v.Marca,
                v.Modelo,
                v.AnioModelo
            FROM Venta venta
            INNER JOIN Vehiculo v ON v.IdVehiculo = venta.IdVehiculo
            WHERE venta.IdCliente = ?
              AND venta.IdVentaEstado = 2
            ORDER BY v.Marca, v.Modelo
            """, (rs, rowNum) -> new VehiculoClienteDto(
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("AnioModelo")
            ), idCliente);
    }

    @GetMapping("/reparaciones")
    public List<ReparacionDto> listarReparaciones(@RequestParam(defaultValue = "activas") String estado) {
        var estados = "completadas".equalsIgnoreCase(estado)
            ? List.of(3)
            : List.of(1, 2);

        return namedJdbc.query("""
            SELECT
                r.IdVehiculoReparacion,
                r.IdVehiculo,
                v.Marca,
                v.Modelo,
                r.IdCliente,
                clientePersona.Nombre AS Cliente,
                r.IdMecanico,
                mecanicoPersona.Nombre AS Mecanico,
                r.IdReparacionEstado,
                estado.DescripcionEs AS Estado,
                r.FechaIngreso,
                r.FechaSalida,
                r.DescripcionProblema,
                r.CostoEstimado,
                r.CostoFinal
            FROM VehiculoReparacion r
            INNER JOIN Vehiculo v ON v.IdVehiculo = r.IdVehiculo
            INNER JOIN Cliente c ON c.IdCliente = r.IdCliente
            INNER JOIN Persona clientePersona ON clientePersona.IdPersona = c.IdPersona
            INNER JOIN Mecanico m ON m.IdMecanico = r.IdMecanico
            INNER JOIN Persona mecanicoPersona ON mecanicoPersona.IdPersona = m.IdPersona
            INNER JOIN ReparacionEstado estado ON estado.IdReparacionEstado = r.IdReparacionEstado
            WHERE r.IdReparacionEstado IN (:estados)
            ORDER BY r.FechaIngreso DESC
            """, new MapSqlParameterSource("estados", estados), (rs, rowNum) -> new ReparacionDto(
                rs.getInt("IdVehiculoReparacion"),
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("IdCliente"),
                rs.getString("Cliente"),
                rs.getInt("IdMecanico"),
                rs.getString("Mecanico"),
                rs.getInt("IdReparacionEstado"),
                rs.getString("Estado"),
                rs.getObject("FechaIngreso", LocalDateTime.class),
                rs.getObject("FechaSalida", LocalDateTime.class),
                rs.getString("DescripcionProblema"),
                rs.getBigDecimal("CostoEstimado"),
                rs.getBigDecimal("CostoFinal")
            ));
    }

    @PostMapping("/reparaciones")
    @Transactional
    public ReparacionDto registrarReparacion(@RequestBody ReparacionRequest request) {
        if (request.idCliente() == null || request.idVehiculo() == null || isBlank(request.descripcionProblema())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente, vehiculo y problema son obligatorios.");
        }

        var idMecanico = request.idMecanico() != null ? request.idMecanico() : obtenerPrimerMecanicoActivo();
        var fechaIngreso = request.fechaIngreso() != null ? request.fechaIngreso().atStartOfDay() : LocalDateTime.now();
        var fechaSalida = request.fechaSalida() != null ? request.fechaSalida().atStartOfDay() : null;
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                INSERT INTO VehiculoReparacion (
                    IdVehiculo,
                    IdMecanico,
                    IdCliente,
                    IdReparacionEstado,
                    FechaIngreso,
                    FechaSalida,
                    DescripcionProblema,
                    CostoEstimado,
                    CostoFinal
                )
                VALUES (?, ?, ?, 1, ?, ?, ?, ?, NULL)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setInt(1, request.idVehiculo());
            ps.setInt(2, idMecanico);
            ps.setInt(3, request.idCliente());
            ps.setObject(4, fechaIngreso);
            ps.setObject(5, fechaSalida);
            ps.setString(6, request.descripcionProblema());
            ps.setBigDecimal(7, request.costoEstimado());
            return ps;
        }, keyHolder);

        var idReparacion = Objects.requireNonNull(keyHolder.getKey()).intValue();
        if (!isBlank(request.diagnosticoInicial())) {
            jdbcTemplate.update("""
                INSERT INTO VehiculoDiagnostico (IdVehiculoReparacion, Descripcion)
                VALUES (?, ?)
                """, idReparacion, request.diagnosticoInicial());
        }

        return obtenerReparacion(idReparacion);
    }

    @PutMapping("/reparaciones/{id}")
    @Transactional
    public ReparacionDto actualizarReparacion(@PathVariable int id, @RequestBody ReparacionUpdateRequest request) {
        obtenerReparacion(id);

        var idMecanico = request.idMecanico() != null ? request.idMecanico() : null;
        var costoEstimado = request.costoEstimado();
        var descripcionProblema = request.descripcionProblema();

        var sqlBuilder = new StringBuilder("""
            UPDATE VehiculoReparacion
            SET """);

        var updates = new java.util.ArrayList<Object>();
        if (idMecanico != null) {
            updates.add(idMecanico);
            sqlBuilder.append("IdMecanico = ?, ");
        }
        if (costoEstimado != null) {
            updates.add(costoEstimado);
            sqlBuilder.append("CostoEstimado = ?, ");
        }
        if (descripcionProblema != null && !descripcionProblema.isBlank()) {
            updates.add(descripcionProblema);
            sqlBuilder.append("DescripcionProblema = ?, ");
        }

        if (updates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay campos para actualizar.");
        }

        updates.add(id);
        sqlBuilder.append("FechaModificacion = SYSUTCDATETIME() WHERE IdVehiculoReparacion = ? AND IdReparacionEstado IN (1, 2)");

        var updated = jdbcTemplate.update(sqlBuilder.toString(), updates.toArray());

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reparacion activa no encontrada.");
        }

        return obtenerReparacion(id);
    }

    @PatchMapping("/reparaciones/{id}/completar")
    public Mensaje completarReparacion(@PathVariable int id, @RequestBody(required = false) CompletarRequest request) {
        var costoFinal = request == null ? null : request.costoFinal();
        var updated = jdbcTemplate.update("""
            UPDATE VehiculoReparacion
            SET IdReparacionEstado = 3,
                FechaSalida = COALESCE(FechaSalida, SYSUTCDATETIME()),
                CostoFinal = COALESCE(?, CostoFinal, CostoEstimado)
            WHERE IdVehiculoReparacion = ?
              AND IdReparacionEstado IN (1, 2)
            """, costoFinal, id);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reparacion activa no encontrada.");
        }

        return new Mensaje("Reparacion completada correctamente.");
    }

    @PatchMapping("/reparaciones/{id}/cancelar")
    public Mensaje cancelarReparacion(@PathVariable int id) {
        var updated = jdbcTemplate.update("""
            UPDATE VehiculoReparacion
            SET IdReparacionEstado = 4
            WHERE IdVehiculoReparacion = ?
              AND IdReparacionEstado IN (1, 2)
            """, id);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reparacion activa no encontrada.");
        }

        return new Mensaje("Reparacion cancelada correctamente.");
    }

    private ReparacionDto obtenerReparacion(int id) {
        return namedJdbc.query("""
            SELECT
                r.IdVehiculoReparacion,
                r.IdVehiculo,
                v.Marca,
                v.Modelo,
                r.IdCliente,
                clientePersona.Nombre AS Cliente,
                r.IdMecanico,
                mecanicoPersona.Nombre AS Mecanico,
                r.IdReparacionEstado,
                estado.DescripcionEs AS Estado,
                r.FechaIngreso,
                r.FechaSalida,
                r.DescripcionProblema,
                r.CostoEstimado,
                r.CostoFinal
            FROM VehiculoReparacion r
            INNER JOIN Vehiculo v ON v.IdVehiculo = r.IdVehiculo
            INNER JOIN Cliente c ON c.IdCliente = r.IdCliente
            INNER JOIN Persona clientePersona ON clientePersona.IdPersona = c.IdPersona
            INNER JOIN Mecanico m ON m.IdMecanico = r.IdMecanico
            INNER JOIN Persona mecanicoPersona ON mecanicoPersona.IdPersona = m.IdPersona
            INNER JOIN ReparacionEstado estado ON estado.IdReparacionEstado = r.IdReparacionEstado
            WHERE r.IdVehiculoReparacion = :id
            """, new MapSqlParameterSource("id", id), (rs, rowNum) -> new ReparacionDto(
                rs.getInt("IdVehiculoReparacion"),
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("IdCliente"),
                rs.getString("Cliente"),
                rs.getInt("IdMecanico"),
                rs.getString("Mecanico"),
                rs.getInt("IdReparacionEstado"),
                rs.getString("Estado"),
                rs.getObject("FechaIngreso", LocalDateTime.class),
                rs.getObject("FechaSalida", LocalDateTime.class),
                rs.getString("DescripcionProblema"),
                rs.getBigDecimal("CostoEstimado"),
                rs.getBigDecimal("CostoFinal")
            )).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Reparacion no encontrada."));
    }

    private int obtenerPrimerMecanicoActivo() {
        return jdbcTemplate.query("""
            SELECT TOP 1 IdMecanico
            FROM Mecanico
            WHERE EstaActivo = 1
            ORDER BY IdMecanico
            """, rs -> {
            if (!rs.next()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay mecanicos activos.");
            }
            return rs.getInt("IdMecanico");
        });
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record MecanicoDto(Integer id, String nombre, String especializacion) {
    }

    public record VehiculoClienteDto(Integer id, String marca, String modelo, Integer anioModelo) {
    }

    public record ReparacionDto(
        Integer id,
        Integer idVehiculo,
        String marca,
        String modelo,
        Integer idCliente,
        String cliente,
        Integer idMecanico,
        String mecanico,
        Integer idEstado,
        String estado,
        LocalDateTime fechaIngreso,
        LocalDateTime fechaSalida,
        String descripcionProblema,
        BigDecimal costoEstimado,
        BigDecimal costoFinal
    ) {
    }

    public record ReparacionRequest(
        Integer idVehiculo,
        Integer idCliente,
        Integer idMecanico,
        LocalDate fechaIngreso,
        LocalDate fechaSalida,
        String descripcionProblema,
        String diagnosticoInicial,
        BigDecimal costoEstimado
    ) {
    }

    public record CompletarRequest(BigDecimal costoFinal) {
    }

    public record ReparacionUpdateRequest(
        Integer idMecanico,
        BigDecimal costoEstimado,
        String descripcionProblema
    ) {
    }

    public record Mensaje(String mensaje) {
    }
}
