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

    @GetMapping("/reparaciones/{id}")
    public ReparacionDto obtenerReparacionPorId(@PathVariable int id) {
        return obtenerReparacion(id);
    }

    @PostMapping("/reparaciones")
    @Transactional
    public ReparacionDto registrarReparacion(@RequestBody ReparacionRequest request) {
        if (request == null || request.idCliente() == null || request.idVehiculo() == null || isBlank(request.descripcionProblema())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cliente, vehiculo y problema son obligatorios.");
        }
        if (request.costoEstimado() != null && request.costoEstimado().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo estimado no puede ser negativo.");
        }
        if (request.fechaSalida() != null && request.fechaIngreso() != null && request.fechaSalida().isBefore(request.fechaIngreso())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La fecha de salida no puede ser anterior a la fecha de entrada.");
        }
        if (!vehiculoPerteneceACliente(request.idCliente(), request.idVehiculo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El vehiculo no pertenece a una venta completada del cliente.");
        }

        var idMecanico = request.idMecanico() != null ? request.idMecanico() : obtenerPrimerMecanicoActivo();
        if (!mecanicoActivo(idMecanico)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mecanico no encontrado o inactivo.");
        }
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

        var clauses = new java.util.ArrayList<String>();
        var updates = new java.util.ArrayList<Object>();
        if (idMecanico != null) {
            if (!mecanicoActivo(idMecanico)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mecanico no encontrado o inactivo.");
            }
            clauses.add("IdMecanico = ?");
            updates.add(idMecanico);
        }
        if (costoEstimado != null) {
            if (costoEstimado.signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo estimado no puede ser negativo.");
            }
            clauses.add("CostoEstimado = ?");
            updates.add(costoEstimado);
        }
        if (descripcionProblema != null && !descripcionProblema.isBlank()) {
            clauses.add("DescripcionProblema = ?");
            updates.add(descripcionProblema);
        }

        if (updates.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No hay campos para actualizar.");
        }

        updates.add(id);
        var sql = "UPDATE VehiculoReparacion SET " + String.join(", ", clauses)
            + " WHERE IdVehiculoReparacion = ? AND IdReparacionEstado IN (1, 2)";

        var updated = jdbcTemplate.update(sql, updates.toArray());

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Reparacion activa no encontrada.");
        }

        return obtenerReparacion(id);
    }

    @PatchMapping("/reparaciones/{id}/completar")
    public Mensaje completarReparacion(@PathVariable int id, @RequestBody(required = false) CompletarRequest request) {
        var costoFinal = request == null ? null : request.costoFinal();
        if (costoFinal != null && costoFinal.signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo final no puede ser negativo.");
        }
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

    private boolean vehiculoPerteneceACliente(int idCliente, int idVehiculo) {
        var total = jdbcTemplate.queryForObject("""
            SELECT COUNT(1)
            FROM Venta
            WHERE IdCliente = ?
              AND IdVehiculo = ?
              AND IdVentaEstado = 2
            """, Integer.class, idCliente, idVehiculo);
        return total != null && total > 0;
    }

    private boolean mecanicoActivo(int idMecanico) {
        var total = jdbcTemplate.queryForObject(
            "SELECT COUNT(1) FROM Mecanico WHERE IdMecanico = ? AND EstaActivo = 1",
            Integer.class,
            idMecanico
        );
        return total != null && total > 0;
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
