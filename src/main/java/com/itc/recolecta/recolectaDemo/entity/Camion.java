package com.itc.recolecta.recolectaDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "camiones")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Camion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "truck_id", unique = true, nullable = false)
    private Integer truckId;

    @Column(name = "capacidad_litros")
    private Double capacidadLitros;

    // km por litro para calcular combustible
    @Column(name = "rendimiento_km_litro")
    private Double rendimientoKmLitro;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}