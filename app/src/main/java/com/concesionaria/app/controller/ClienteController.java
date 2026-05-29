package com.concesionaria.app.controller;

import com.concesionaria.app.dto.ClienteResumenDto;
import com.concesionaria.app.repository.ClienteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.CrossOrigin;
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

@RestController
@RequestMapping("/api/clientes")
@CrossOrigin(origins = "*")
public class ClienteController {

    private final ClienteRepository repo;
    private final JdbcTemplate jdbcTemplate;

    public ClienteController(ClienteRepository repo, JdbcTemplate jdbcTemplate) {
        this.repo = repo;
        this.jdbcTemplate = jdbcTemplate;
    }

    @GetMapping
    public List<ClienteResumenDto> listarClientes() {
        return repo.findResumenes();
    }

    @GetMapping("/{id}")
    public ClienteResumenDto obtenerCliente(@PathVariable int id) {
        return jdbcTemplate.query("""
            SELECT
                c.IdCliente,
                p.Nombre,
                p.Domicilio,
                c.EstaActivo
            FROM Cliente c
            INNER JOIN Persona p ON p.IdPersona = c.IdPersona
            WHERE c.IdCliente = ? AND c.EstaActivo = 1
            """, (rs, rowNum) -> new ClienteResumenDto(
                rs.getInt("IdCliente"),
                rs.getString("Nombre"),
                rs.getString("Domicilio"),
                rs.getBoolean("EstaActivo")
            ), id).stream().findFirst()
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Cliente no encontrado."));
    }

    @PostMapping
    @Transactional
    public Mensaje crearCliente(@RequestBody ClienteRequest request) {
        if (isBlank(request.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }

        var nombreCompleto = joinParts(request.nombre(), request.apellidoPaterno(), request.apellidoMaterno());
        var domicilio = buildDomicilio(request.colonia(), request.calle(), request.numExt(), request.telefono());
        var keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            var ps = connection.prepareStatement(
                "INSERT INTO Persona (Nombre, Domicilio) VALUES (?, ?)",
                Statement.RETURN_GENERATED_KEYS
            );
            ps.setString(1, nombreCompleto);
            ps.setString(2, domicilio);
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
        if (isBlank(request.nombre())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nombre es obligatorio.");
        }

        var nombreCompleto = joinParts(request.nombre(), request.apellidoPaterno(), request.apellidoMaterno());
        var domicilio = buildDomicilio(request.colonia(), request.calle(), request.numExt(), request.telefono());
        var updated = jdbcTemplate.update("""
            UPDATE p
            SET p.Nombre = ?, p.Domicilio = ?, p.FechaModificacion = SYSUTCDATETIME()
            FROM Persona p
            INNER JOIN Cliente c ON c.IdPersona = p.IdPersona
            WHERE c.IdCliente = ? AND c.EstaActivo = 1
            """, nombreCompleto, domicilio, id);

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

    private static String buildDomicilio(String colonia, String calle, String numExt, String telefono) {
        var direccion = joinParts(calle, isBlank(numExt) ? null : "No. " + numExt, colonia);
        if (!isBlank(telefono)) {
            return isBlank(direccion) ? "Tel. " + telefono : direccion + " | Tel. " + telefono;
        }
        return direccion;
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
