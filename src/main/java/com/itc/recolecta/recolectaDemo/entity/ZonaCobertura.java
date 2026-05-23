package com.itc.recolecta.recolectaDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "zonas_cobertura")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ZonaCobertura {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_colonia", nullable = false)
    private String nombreColonia;

    @Column(name = "horario_estimado")
    private String horarioEstimado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;
}