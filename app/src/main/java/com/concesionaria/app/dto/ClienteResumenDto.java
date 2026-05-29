package com.concesionaria.app.dto;

public record ClienteResumenDto(
    Integer id,
    String nombreCompleto,
    String domicilio,
    Boolean activo
) {
}
