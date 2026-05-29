// package com.concesionaria.app.controller;

// import org.springframework.http.HttpStatus;
// import org.springframework.jdbc.core.JdbcTemplate;
// import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
// import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
// import org.springframework.jdbc.support.GeneratedKeyHolder;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.bind.annotation.DeleteMapping;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.PutMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import org.springframework.web.bind.annotation.RequestMapping;
// import org.springframework.web.bind.annotation.RequestParam;
// import org.springframework.web.bind.annotation.RestController;
// import org.springframework.web.server.ResponseStatusException;

// import java.math.BigDecimal;
// import java.sql.Statement;
// import java.time.LocalDateTime;
// import java.util.List;
// import java.util.Map;
// import java.util.Objects;

// @RestController
// @RequestMapping("/api/vehiculos")
// public class VehiculoController {

//     private final NamedParameterJdbcTemplate namedJdbc;
//     private final JdbcTemplate jdbcTemplate;

//     public VehiculoController(NamedParameterJdbcTemplate namedJdbc, JdbcTemplate jdbcTemplate) {
//         this.namedJdbc = namedJdbc;
//         this.jdbcTemplate = jdbcTemplate;
//     }

//     @GetMapping
//     public List<VehiculoDto> listarVehiculos(
//         @RequestParam(required = false) String marca,
//         @RequestParam(required = false) String modelo,
//         @RequestParam(required = false) Integer anio,
//         @RequestParam(required = false, defaultValue = "Marca-Ascendente") String sort
//     ) {
//         var params = new MapSqlParameterSource()
//             .addValue("marca", blankToNull(marca))
//             .addValue("modelo", blankToNull(modelo))
//             .addValue("anio", anio);

//         return namedJdbc.query("""
//             SELECT
//                 v.IdVehiculo,
//                 v.Marca,
//                 v.Modelo,
//                 v.AnioModelo,
//                 v.Placas,
//                 v.NumeroSerie,
//                 v.Costo,
//                 v.IdVehiculoCondicion,
//                 vc.DescripcionEs AS Condicion,
//                 v.FechaRegistro,
//                 CAST(CASE WHEN EXISTS (
//                     SELECT 1
//                     FROM Venta venta
//                     WHERE venta.IdVehiculo = v.IdVehiculo
//                       AND venta.IdVentaEstado = 2
//                 ) THEN 0 ELSE 1 END AS bit) AS Disponible
//             FROM Vehiculo v
//             INNER JOIN VehiculoCondicion vc ON vc.IdVehiculoCondicion = v.IdVehiculoCondicion
//             WHERE (:marca IS NULL OR v.Marca = :marca)
//               AND (:modelo IS NULL OR v.Modelo = :modelo)
//               AND (:anio IS NULL OR v.AnioModelo = :anio)
//             """ + orderBy(sort), params, (rs, rowNum) -> new VehiculoDto(
//                 rs.getInt("IdVehiculo"),
//                 rs.getString("Marca"),
//                 rs.getString("Modelo"),
//                 rs.getInt("AnioModelo"),
//                 rs.getString("Placas"),
//                 rs.getString("NumeroSerie"),
//                 rs.getBigDecimal("Costo"),
//                 rs.getInt("IdVehiculoCondicion"),
//                 rs.getString("Condicion"),
//                 rs.getObject("FechaRegistro", LocalDateTime.class),
//                 rs.getBoolean("Disponible")
//             ));
//     }

//     @GetMapping("/{id}")
//     public VehiculoDto obtenerVehiculo(@PathVariable int id) {
//         return namedJdbc.query("""
//             SELECT
//                 v.IdVehiculo,
//                 v.Marca,
//                 v.Modelo,
//                 v.AnioModelo,
//                 v.Placas,
//                 v.NumeroSerie,
//                 v.Costo,
//                 v.IdVehiculoCondicion,
//                 vc.DescripcionEs AS Condicion,
//                 v.FechaRegistro,
//                 CAST(CASE WHEN EXISTS (
//                     SELECT 1 FROM Venta venta
//                     WHERE venta.IdVehiculo = v.IdVehiculo AND venta.IdVentaEstado = 2
//                 ) THEN 0 ELSE 1 END AS bit) AS Disponible
//             FROM Vehiculo v
//             INNER JOIN VehiculoCondicion vc ON vc.IdVehiculoCondicion = v.IdVehiculoCondicion
//             WHERE v.IdVehiculo = :id
//             """, new MapSqlParameterSource("id", id), (rs, rowNum) -> new VehiculoDto(
//                 rs.getInt("IdVehiculo"),
//                 rs.getString("Marca"),
//                 rs.getString("Modelo"),
//                 rs.getInt("AnioModelo"),
//                 rs.getString("Placas"),
//                 rs.getString("NumeroSerie"),
//                 rs.getBigDecimal("Costo"),
//                 rs.getInt("IdVehiculoCondicion"),
//                 rs.getString("Condicion"),
//                 rs.getObject("FechaRegistro", LocalDateTime.class),
//                 rs.getBoolean("Disponible")
//             )).stream().findFirst()
//             .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado."));
//     }

//     @GetMapping("/marcas")
//     public List<String> listarMarcas() {
//         return jdbcTemplate.queryForList("SELECT DISTINCT Marca FROM Vehiculo ORDER BY Marca", String.class);
//     }

//     @GetMapping("/modelos")
//     public List<String> listarModelos(@RequestParam String marca) {
//         return jdbcTemplate.queryForList("""
//             SELECT DISTINCT Modelo
//             FROM Vehiculo
//             WHERE Marca = ?
//             ORDER BY Modelo
//             """, String.class, marca);
//     }

//     @PostMapping
//     @Transactional
//     public VehiculoDto crearVehiculo(@RequestBody VehiculoRequest request) {
//         if (isBlank(request.marca()) || isBlank(request.modelo()) || request.anioModelo() == null) {
//             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca, modelo y anio son obligatorios.");
//         }

//         var condicionId = request.idVehiculoCondicion() != null
//             ? request.idVehiculoCondicion()
//             : condicionId(request.condicion());
//         var keyHolder = new GeneratedKeyHolder();

//         jdbcTemplate.update(connection -> {
//             var ps = connection.prepareStatement("""
//                 INSERT INTO Vehiculo (Marca, Modelo, AnioModelo, Placas, NumeroSerie, Costo, IdVehiculoCondicion)
//                 VALUES (?, ?, ?, ?, ?, ?, ?)
//                 """, Statement.RETURN_GENERATED_KEYS);
//             ps.setString(1, request.marca());
//             ps.setString(2, request.modelo());
//             ps.setInt(3, request.anioModelo());
//             ps.setString(4, blankToNull(request.placas()));
//             ps.setString(5, blankToNull(request.numeroSerie()));
//             ps.setBigDecimal(6, request.costo());
//             ps.setInt(7, condicionId);
//             return ps;
//         }, keyHolder);

//         return obtenerVehiculo(Objects.requireNonNull(keyHolder.getKey()).intValue());
//     }

//     @PutMapping("/{id}")
//     @Transactional
//     public VehiculoDto actualizarVehiculo(@PathVariable int id, @RequestBody VehiculoRequest request) {
//         obtenerVehiculo(id);

//         var condicionId = request.idVehiculoCondicion() != null
//             ? request.idVehiculoCondicion()
//             : condicionId(request.condicion());

//         var updated = jdbcTemplate.update("""
//             UPDATE Vehiculo
//             SET Marca = ?, Modelo = ?, AnioModelo = ?, Placas = ?, NumeroSerie = ?, Costo = ?, IdVehiculoCondicion = ?,
//                 FechaModificacion = SYSUTCDATETIME()
//             WHERE IdVehiculo = ?
//             """, request.marca(), request.modelo(), request.anioModelo(),
//             blankToNull(request.placas()), blankToNull(request.numeroSerie()),
//             request.costo(), condicionId, id);

//         if (updated == 0) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado.");
//         }

//         return obtenerVehiculo(id);
//     }

//     @DeleteMapping("/{id}")
//     public Mensaje desactivarVehiculo(@PathVariable int id) {
//         obtenerVehiculo(id);

//         var updated = jdbcTemplate.update("""
//             UPDATE Vehiculo
//             SET FechaModificacion = SYSUTCDATETIME()
//             WHERE IdVehiculo = ?
//             """, id);

//         if (updated == 0) {
//             throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado.");
//         }

//         return new Mensaje("Vehiculo eliminado correctamente.");
//     }

//     private static String orderBy(String sort) {
//         return " ORDER BY " + Map.ofEntries(
//             Map.entry("Marca-Ascendente", "v.Marca ASC"),
//             Map.entry("Marca-Descendente", "v.Marca DESC"),
//             Map.entry("Modelo-Ascendente", "v.Modelo ASC"),
//             Map.entry("Modelo-Descendente", "v.Modelo DESC"),
//             Map.entry("Precio-Ascendente", "v.Costo ASC"),
//             Map.entry("Precio-Descendente", "v.Costo DESC"),
//             Map.entry("Anio-Ascendente", "v.AnioModelo ASC"),
//             Map.entry("Anio-Descendente", "v.AnioModelo DESC"),
//             Map.entry("Año-Ascendente", "v.AnioModelo ASC"),
//             Map.entry("Año-Descendente", "v.AnioModelo DESC")
//         ).getOrDefault(sort, "v.Marca ASC, v.Modelo ASC");
//     }

//     private static int condicionId(String condicion) {
//         return "Usado".equalsIgnoreCase(condicion) ? 2 : 1;
//     }

//     private static String blankToNull(String value) {
//         return isBlank(value) ? null : value;
//     }

//     private static boolean isBlank(String value) {
//         return value == null || value.isBlank();
//     }

//     public record VehiculoDto(
//         Integer id,
//         String marca,
//         String modelo,
//         Integer anioModelo,
//         String placas,
//         String numeroSerie,
//         BigDecimal costo,
//         Integer idVehiculoCondicion,
//         String condicion,
//         LocalDateTime fechaRegistro,
//         Boolean disponible
//     ) {
//     }

//     public record VehiculoRequest(
//         String marca,
//         String modelo,
//         Integer anioModelo,
//         String placas,
//         String numeroSerie,
//         BigDecimal costo,
//         Integer idVehiculoCondicion,
//         String condicion
//     ) {
//     }

//     public record Mensaje(String mensaje) {
//     }
// }
package com.concesionaria.app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/api/vehiculos")
public class VehiculoController {

    private final NamedParameterJdbcTemplate namedJdbc;
    private final JdbcTemplate jdbcTemplate;

    public VehiculoController(NamedParameterJdbcTemplate namedJdbc, JdbcTemplate jdbcTemplate) {
        this.namedJdbc = namedJdbc;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<VehiculoDto> listarVehiculos(
        @RequestParam(required = false) String marca,
        @RequestParam(required = false) String modelo,
        @RequestParam(required = false) Integer anio,
        @RequestParam(required = false, defaultValue = "Marca-Ascendente") String sort
    ) {
        var params = new MapSqlParameterSource()
            .addValue("marca", blankToNull(marca))
            .addValue("modelo", blankToNull(modelo))
            .addValue("anio", anio);

        return namedJdbc.query("""
            SELECT
                v.IdVehiculo,
                v.Marca,
                v.Modelo,
                v.AnioModelo,
                v.Placas,
                v.NumeroSerie,
                v.Costo,
                v.IdVehiculoCondicion,
                vc.DescripcionEs AS Condicion,
                v.FechaRegistro,
                CAST(CASE WHEN EXISTS (
                    SELECT 1
                    FROM Venta venta
                    WHERE venta.IdVehiculo = v.IdVehiculo
                      AND venta.IdVentaEstado = 2
                ) THEN 0 ELSE 1 END AS bit) AS Disponible,
                vi.RutaImagen AS ImagenPrincipal
            FROM Vehiculo v
            INNER JOIN VehiculoCondicion vc ON vc.IdVehiculoCondicion = v.IdVehiculoCondicion
            LEFT JOIN VehiculoImagen vi ON vi.IdVehiculo = v.IdVehiculo AND vi.EsPrincipal = 1
            WHERE (:marca IS NULL OR v.Marca = :marca)
              AND (:modelo IS NULL OR v.Modelo = :modelo)
              AND (:anio IS NULL OR v.AnioModelo = :anio)
            """ + orderBy(sort), params, (rs, rowNum) -> new VehiculoDto(
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("AnioModelo"),
                rs.getString("Placas"),
                rs.getString("NumeroSerie"),
                rs.getBigDecimal("Costo"),
                rs.getInt("IdVehiculoCondicion"),
                rs.getString("Condicion"),
                rs.getObject("FechaRegistro", LocalDateTime.class),
                rs.getBoolean("Disponible"),
                rs.getString("ImagenPrincipal")
            ));
    }

    @GetMapping("/{id}")
    public VehiculoDto obtenerVehiculo(@PathVariable int id) {
        return namedJdbc.query("""
            SELECT
                v.IdVehiculo,
                v.Marca,
                v.Modelo,
                v.AnioModelo,
                v.Placas,
                v.NumeroSerie,
                v.Costo,
                v.IdVehiculoCondicion,
                vc.DescripcionEs AS Condicion,
                v.FechaRegistro,
                CAST(CASE WHEN EXISTS (
                    SELECT 1 FROM Venta venta
                    WHERE venta.IdVehiculo = v.IdVehiculo AND venta.IdVentaEstado = 2
                ) THEN 0 ELSE 1 END AS bit) AS Disponible,
                vi.RutaImagen AS ImagenPrincipal
            FROM Vehiculo v
            INNER JOIN VehiculoCondicion vc ON vc.IdVehiculoCondicion = v.IdVehiculoCondicion
            LEFT JOIN VehiculoImagen vi ON vi.IdVehiculo = v.IdVehiculo AND vi.EsPrincipal = 1
            WHERE v.IdVehiculo = :id
            """, new MapSqlParameterSource("id", id), (rs, rowNum) -> new VehiculoDto(
                rs.getInt("IdVehiculo"),
                rs.getString("Marca"),
                rs.getString("Modelo"),
                rs.getInt("AnioModelo"),
                rs.getString("Placas"),
                rs.getString("NumeroSerie"),
                rs.getBigDecimal("Costo"),
                rs.getInt("IdVehiculoCondicion"),
                rs.getString("Condicion"),
                rs.getObject("FechaRegistro", LocalDateTime.class),
                rs.getBoolean("Disponible"),
                rs.getString("ImagenPrincipal")
            )).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado."));
    }

    @GetMapping("/marcas")
    public List<String> listarMarcas() {
        return jdbcTemplate.queryForList("SELECT DISTINCT Marca FROM Vehiculo ORDER BY Marca", String.class);
    }

    @GetMapping("/modelos")
    public List<String> listarModelos(@RequestParam String marca) {
        return jdbcTemplate.queryForList("""
            SELECT DISTINCT Modelo
            FROM Vehiculo
            WHERE Marca = ?
            ORDER BY Modelo
            """, String.class, marca);
    }

    @PostMapping
    @Transactional
    public VehiculoDto crearVehiculo(@RequestBody VehiculoRequest request) {
        validarVehiculoRequest(request);

        var condicionId = request.idVehiculoCondicion() != null
            ? request.idVehiculoCondicion()
            : condicionId(request.condicion());
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement("""
                INSERT INTO Vehiculo (Marca, Modelo, AnioModelo, Placas, NumeroSerie, Costo, IdVehiculoCondicion)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, request.marca());
            ps.setString(2, request.modelo());
            ps.setInt(3, request.anioModelo());
            ps.setString(4, blankToNull(request.placas()));
            ps.setString(5, blankToNull(request.numeroSerie()));
            ps.setBigDecimal(6, request.costo());
            ps.setInt(7, condicionId);
            return ps;
        }, keyHolder);

        return obtenerVehiculo(Objects.requireNonNull(keyHolder.getKey()).intValue());
    }

    @PutMapping("/{id}")
    @Transactional
    public VehiculoDto actualizarVehiculo(@PathVariable int id, @RequestBody VehiculoRequest request) {
        obtenerVehiculo(id);
        validarVehiculoRequest(request);

        var condicionId = request.idVehiculoCondicion() != null
            ? request.idVehiculoCondicion()
            : condicionId(request.condicion());

        var updated = jdbcTemplate.update("""
            UPDATE Vehiculo
            SET Marca = ?, Modelo = ?, AnioModelo = ?, Placas = ?, NumeroSerie = ?, Costo = ?, IdVehiculoCondicion = ?
            WHERE IdVehiculo = ?
            """, request.marca(), request.modelo(), request.anioModelo(),
            blankToNull(request.placas()), blankToNull(request.numeroSerie()),
            request.costo(), condicionId, id);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado.");
        }

        return obtenerVehiculo(id);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public Mensaje desactivarVehiculo(@PathVariable int id) {
        obtenerVehiculo(id);

        if (tieneReferencias(id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "El vehiculo tiene ventas o reparaciones relacionadas; no se puede eliminar fisicamente."
            );
        }

        jdbcTemplate.update("DELETE FROM VehiculoImagen WHERE IdVehiculo = ?", id);
        var updated = jdbcTemplate.update("DELETE FROM Vehiculo WHERE IdVehiculo = ?", id);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehiculo no encontrado.");
        }

        return new Mensaje("Vehiculo eliminado correctamente.");
    }

    private boolean tieneReferencias(int id) {
        var referencias = jdbcTemplate.queryForObject("""
            SELECT
                (SELECT COUNT(1) FROM Venta WHERE IdVehiculo = ?) +
                (SELECT COUNT(1) FROM VehiculoReparacion WHERE IdVehiculo = ?)
            """, Integer.class, id, id);
        return referencias != null && referencias > 0;
    }

    private static void validarVehiculoRequest(VehiculoRequest request) {
        if (isBlank(request.marca()) || isBlank(request.modelo()) || request.anioModelo() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Marca, modelo y anio son obligatorios.");
        }
        if (request.anioModelo() < 1900 || request.anioModelo() > 2100) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El anio del vehiculo debe estar entre 1900 y 2100.");
        }
        if (request.costo() != null && request.costo().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El costo no puede ser negativo.");
        }
        if (request.idVehiculoCondicion() != null && request.idVehiculoCondicion() != 1 && request.idVehiculoCondicion() != 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La condicion del vehiculo no es valida.");
        }
    }

    private static String orderBy(String sort) {
        return " ORDER BY " + Map.ofEntries(
            Map.entry("Marca-Ascendente", "v.Marca ASC"),
            Map.entry("Marca-Descendente", "v.Marca DESC"),
            Map.entry("Modelo-Ascendente", "v.Modelo ASC"),
            Map.entry("Modelo-Descendente", "v.Modelo DESC"),
            Map.entry("Precio-Ascendente", "v.Costo ASC"),
            Map.entry("Precio-Descendente", "v.Costo DESC"),
            Map.entry("Anio-Ascendente", "v.AnioModelo ASC"),
            Map.entry("Anio-Descendente", "v.AnioModelo DESC"),
            Map.entry("Año-Ascendente", "v.AnioModelo ASC"),
            Map.entry("Año-Descendente", "v.AnioModelo DESC")
        ).getOrDefault(sort, "v.Marca ASC, v.Modelo ASC");
    }

    private static int condicionId(String condicion) {
        return "Usado".equalsIgnoreCase(condicion) ? 2 : 1;
    }

    private static String blankToNull(String value) {
        return isBlank(value) ? null : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    public record VehiculoDto(
        Integer id,
        String marca,
        String modelo,
        Integer anioModelo,
        String placas,
        String numeroSerie,
        BigDecimal costo,
        Integer idVehiculoCondicion,
        String condicion,
        LocalDateTime fechaRegistro,
        Boolean disponible,
        String imagenPrincipal 
    ) {
    }

    public record VehiculoRequest(
        String marca,
        String modelo,
        Integer anioModelo,
        String placas,
        String numeroSerie,
        BigDecimal costo,
        Integer idVehiculoCondicion,
        String condicion
    ) {
    }

    public record Mensaje(String mensaje) {
    }
}
