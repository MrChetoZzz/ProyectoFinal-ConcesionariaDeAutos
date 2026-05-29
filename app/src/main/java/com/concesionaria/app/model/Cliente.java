package com.concesionaria.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "Cliente")
@Data
public class Cliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IdCliente")
    private Integer id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "IdPersona", nullable = false)
    private Persona persona;

    @Column(name = "EstaActivo", nullable = false)
    private Boolean estaActivo;

    @Column(name = "FechaIngreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "FechaModificacion")
    private LocalDateTime fechaModificacion;
}
