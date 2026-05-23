package com.itc.recolecta.recolectaDemo.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "evaluaciones_ruta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluacionRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camionero_id", nullable = false)
    private Usuario camionero;

    @Column(name = "llego_a_tiempo")
    private Boolean llegoATiempo;

    @Column(name = "tuvo_incidencia")
    private Boolean tuvoIncidencia;

    // 1 a 5 estrellas del ciudadano
    @Column(name = "calificacion_ciudadano")
    private Integer calificacionCiudadano;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}