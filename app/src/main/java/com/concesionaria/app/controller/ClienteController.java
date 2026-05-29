package com.concesionaria.app.controller;

import com.concesionaria.app.dto.ClienteResumenDto;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final JdbcTemplate jdbcTemplate;

    public ClienteController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<ClienteResumenDto> listarClientes() {
        return jdbcTemplate.query("""
            SELECT
                c.IdCliente,
                p.Nombre,
                p.Domicilio,
                CONVERT(varchar(18), p.CurpCifrada) AS Curp,
                c.EstaActivo
            FROM Cliente c
            INNER JOIN Persona p ON p.IdPersona = c.IdPersona
            WHERE c.EstaActivo = 1
            ORDER BY c.IdCliente
            """, (rs, rowNum) -> mapCliente(
                rs.getInt("IdCliente"),
                rs.getString("Nombre"),
                rs.getString("Domicilio"),
                rs.getString("Curp"),
                rs.getBoolean("EstaActivo")
            ));
    }

    @GetMapping("/{id}")
    public ClienteResumenDto obtenerCliente(@PathVariable int id) {
        return jdbcTemplate.query("""
            SELECT
                c.IdCliente,
                p.Nombre,
                p.Domicilio,
                CONVERT(varchar(18), p.CurpCifrada) AS Curp,
                c.EstaActivo
            FROM Cliente c
            INNER JOIN Persona p ON p.IdPersona = c.IdPersona
            WHERE c.IdCliente = ? AND c.EstaActivo = 1
            """, (rs, rowNum) -> mapCliente(
                rs.getInt("IdCliente"),
                rs.getString("Nombre"),
                rs.getString("Domicilio"),
                rs.getString("Curp"),
                rs.getBoolean("EstaActivo")
            ), id).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
    }

    @PostMapping
    @Transactional
    public Mensaje crearCliente(@RequestBody ClienteRequest request) {
        validarClienteRequest(request);

        var nombreCompleto = joinParts(request.nombre(), request.apellidoPaterno(), request.apellidoMaterno());
        var domicilio = buildDomicilio(request.colonia(), request.calle(), request.numExt(), request.telefono());
        var curp = normalizeCurp(request.curp());
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                """
                INSERT INTO Persona (Nombre, Domicilio, CurpCifrada, CurpHash)
                VALUES (?, ?, CONVERT(varbinary(256), CONVERT(varchar(18), ?)), HASHBYTES('SHA2_256', UPPER(?)))
                """,
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, nombreCompleto);
            ps.setString(2, domicilio);
            ps.setString(3, curp);
            ps.setString(4, curp);
            return ps;
        }, keyHolder);

        var idPersona = Objects.requireNonNull(keyHolder.getKey()).intValue();
        jdbcTemplate.update(
            "INSERT INTO Cliente (IdPersona, EstaActivo) VALUES (?, 1)",
            idPersona
        );

        return new Mensaje("Cliente registrado correctamente.");
    }

    @PutMapping("/{id}")
    @Transactional
    public Mensaje actualizarCliente(@PathVariable int id, @RequestBody ClienteRequest request) {
        validarClienteRequest(request);

        var nombreCompleto = joinParts(request.nombre(), request.apellidoPaterno(), request.apellidoMaterno());
        var domicilio = buildDomicilio(request.colonia(), request.calle(), request.numExt(), request.telefono());
        var curp = normalizeCurp(request.curp());
        var updated = jdbcTemplate.update("""
            UPDATE p
            SET p.Nombre = ?,
                p.Domicilio = ?,
                p.CurpCifrada = CONVERT(varbinary(256), CONVERT(varchar(18), ?)),
                p.CurpHash = HASHBYTES('SHA2_256', UPPER(?)),
                p.FechaModificacion = SYSUTCDATETIME()
            FROM Persona p
            INNER JOIN Cliente c ON c.IdPersona = p.IdPersona
            WHERE c.IdCliente = ? AND c.EstaActivo = 1
            """, nombreCompleto, domicilio, curp, curp, id);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado.");
        }

        return new Mensaje("Cliente actualizado correctamente.");
    }

    @DeleteMapping("/{id}")
    public Mensaje desactivarCliente(@PathVariable int id) {
        var updated = jdbcTemplate.update("""
            UPDATE Cliente
            SET EstaActivo = 0, FechaModificacion = SYSUTCDATETIME()
            WHERE IdCliente = ? AND EstaActivo = 1
            """, id);

        if (updated == 0) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado.");
        }

        return new Mensaje("Cliente desactivado correctamente.");
    }

    private static void validarClienteRequest(ClienteRequest request) {
        if (request == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Los datos del cliente son obligatorios.");
        }
        if (isBlank(request.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }
        if (isBlank(request.apellidoPaterno())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El apellido paterno es obligatorio.");
        }
        if (isBlank(request.apellidoMaterno())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El apellido materno es obligatorio.");
        }
        if (isBlank(request.telefono()) || !request.telefono().trim().matches("\\d{10}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El telefono debe tener 10 digitos.");
        }
        if (isBlank(request.curp()) || !request.curp().trim().toUpperCase().matches("[A-Z0-9]{18}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La CURP debe tener exactamente 18 caracteres alfanumericos.");
        }
        if (isBlank(request.colonia())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La colonia es obligatoria.");
        }
        if (isBlank(request.calle())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "La calle es obligatoria.");
        }
        if (isBlank(request.numExt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El numero exterior es obligatorio.");
        }
        if (request.numExt().trim().length() > 12) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El numero exterior no puede exceder 12 caracteres.");
        }
    }

    private static String buildDomicilio(String colonia, String calle, String numExt, String telefono) {
        var parts = new java.util.ArrayList<String>();
        if (!isBlank(calle)) {
            parts.add("Calle: " + calle.trim());
        }
        if (!isBlank(numExt)) {
            parts.add("Num. Ext.: " + numExt.trim());
        }
        if (!isBlank(colonia)) {
            parts.add("Colonia: " + colonia.trim());
        }
        if (!isBlank(telefono)) {
            parts.add("Tel.: " + telefono.trim());
        }
        return String.join(" | ", parts);
    }

    private static String joinParts(String... parts) {
        return java.util.Arrays.stream(parts)
            .filter(part -> !isBlank(part))
            .map(String::trim)
            .reduce((left, right) -> left + " " + right)
            .orElse("");
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String normalizeCurp(String value) {
        return value == null ? null : value.trim().toUpperCase();
    }

    private static ClienteResumenDto mapCliente(int id, String nombreCompleto, String domicilio, String curp, boolean activo) {
        var nombre = parseNombre(nombreCompleto);
        var direccion = parseDomicilio(domicilio);
        return new ClienteResumenDto(
            id,
            nombreCompleto,
            domicilio,
            activo,
            nombre.nombre(),
            nombre.apellidoPaterno(),
            nombre.apellidoMaterno(),
            direccion.telefono(),
            curp,
            direccion.colonia(),
            direccion.calle(),
            direccion.numExt()
        );
    }

    private static NombreParts parseNombre(String nombreCompleto) {
        if (isBlank(nombreCompleto)) {
            return new NombreParts("", "", "");
        }
        var partes = nombreCompleto.trim().split("\\s+");
        if (partes.length == 1) {
            return new NombreParts(partes[0], "", "");
        }
        if (partes.length == 2) {
            return new NombreParts(partes[0], partes[1], "");
        }
        var nombreEnd = Math.max(1, partes.length - 2);
        var nombre = String.join(" ", java.util.Arrays.copyOfRange(partes, 0, nombreEnd));
        var apellidoPaterno = partes[partes.length - 2];
        var apellidoMaterno = partes[partes.length - 1];
        return new NombreParts(nombre, apellidoPaterno, apellidoMaterno);
    }

    private static DomicilioParts parseDomicilio(String domicilio) {
        if (isBlank(domicilio)) {
            return new DomicilioParts("", "", "", "");
        }

        var calle = "";
        var colonia = "";
        var numExt = "";
        var telefono = "";
        for (var part : domicilio.split("\\s*\\|\\s*")) {
            var clean = part.trim();
            if (startsWithLabel(clean, "Calle:")) {
                calle = clean.substring("Calle:".length()).trim();
            } else if (startsWithLabel(clean, "Num. Ext.:")) {
                numExt = clean.substring("Num. Ext.:".length()).trim();
            } else if (startsWithLabel(clean, "Colonia:")) {
                colonia = clean.substring("Colonia:".length()).trim();
            } else if (startsWithLabel(clean, "Tel.:") || startsWithLabel(clean, "Tel.")) {
                telefono = clean.replaceFirst("(?i)^Tel\\.?\\s*:?\\s*", "").trim();
            }
        }

        if (!isBlank(calle) || !isBlank(colonia) || !isBlank(numExt) || !isBlank(telefono)) {
            return new DomicilioParts(colonia, calle, numExt, telefono);
        }

        var sinTelefono = domicilio;
        var telMatcher = Pattern.compile("\\|\\s*Tel\\.?\\s*(\\d{10})", Pattern.CASE_INSENSITIVE).matcher(sinTelefono);
        if (telMatcher.find()) {
            telefono = telMatcher.group(1);
            sinTelefono = sinTelefono.substring(0, telMatcher.start()).trim();
        }

        var numMatcher = Pattern.compile("\\s+No\\.\\s+([^\\s,|]+)\\s*").matcher(sinTelefono);
        if (numMatcher.find()) {
            calle = sinTelefono.substring(0, numMatcher.start()).trim();
            numExt = numMatcher.group(1).trim();
            colonia = sinTelefono.substring(numMatcher.end()).trim();
        } else {
            calle = sinTelefono.trim();
        }

        return new DomicilioParts(colonia, calle, numExt, telefono);
    }

    private static boolean startsWithLabel(String value, String label) {
        return value.regionMatches(true, 0, label, 0, label.length());
    }

    private record NombreParts(String nombre, String apellidoPaterno, String apellidoMaterno) {
    }

    private record DomicilioParts(String colonia, String calle, String numExt, String telefono) {
    }

    public record ClienteRequest(
        String nombre,
        String apellidoPaterno,
        String apellidoMaterno,
        String telefono,
        String curp,
        String colonia,
        String calle,
        String numExt
    ) {
    }

    public record Mensaje(String mensaje) {
    }
}
