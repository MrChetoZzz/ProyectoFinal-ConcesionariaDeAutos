package com.concesionaria.app.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
@CrossOrigin(origins = "*")
public class DatabaseController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/db-check")
    public String checkConnection() {
        try {
            jdbcTemplate.execute("SELECT 1");
            return "¡Conexión exitosa a SQL Server desde Spring Boot!";
        } catch (Exception e) {
            return "Error al conectar: " + e.getMessage();
        }
    }
}