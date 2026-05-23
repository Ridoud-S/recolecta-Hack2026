package com.itc.recolecta.recolectaDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "domicilios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Domicilio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // "Casa", "Trabajo", etc
    @Column(nullable = false)
    private String alias;

    @Column(nullable = false)
    private String calle;

    @Column(nullable = false)
    private String colonia;

    @Column(name = "codigo_postal")
    private String codigoPostal;

    // Guardado one-time desde Nominatim
    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    // Se asigna al validar el domicilio
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "zona_cobertura_id")
    private ZonaCobertura zonaCobertura;

    @Builder.Default
    private Boolean activo = true;
}
