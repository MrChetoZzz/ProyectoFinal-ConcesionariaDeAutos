package com.concesionaria.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-check")
    public ResponseEntity<DbCheckResponse> checkConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return ResponseEntity.ok(new DbCheckResponse("ok", "Conexion exitosa a SQL Server."));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new DbCheckResponse("error", "No se pudo conectar a SQL Server."));
        }
    }

    public record DbCheckResponse(String status, String mensaje) {
    }
}
