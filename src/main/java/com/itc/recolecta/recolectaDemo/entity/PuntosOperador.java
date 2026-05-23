package com.itc.recolecta.recolectaDemo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "puntos_operador")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PuntosOperador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @ManyToOne
    @JoinColumn(name = "camionero_id", nullable = false)
    private Usuario camionero;

    @Builder.Default
    private Integer puntosTotales = 0;

    @Builder.Default
    private Integer rutasCompletadas = 0;

    @Builder.Default
    private Integer rutasATiempo = 0;

    @Builder.Default
    private Integer incidenciasReportadas = 0;

    // Mes y año para ranking mensual
    private Integer mes;
    private Integer anio;
}
