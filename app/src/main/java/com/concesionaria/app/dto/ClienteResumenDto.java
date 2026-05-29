package com.concesionaria.app.dto;

public record ClienteResumenDto(
    Integer id,
    String nombreCompleto,
    String domicilio,
    Boolean activo,
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
