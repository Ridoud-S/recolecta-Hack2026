package com.itc.recolecta.recolectaDemo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "posiciones_ruta")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PosicionRuta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "position_id", nullable = false)
    private Integer positionId;

    @Column(nullable = false)
    private Double lat;

    @Column(nullable = false)
    private Double lng;

    private Double speed;

    private LocalDateTime timestamp;

    // orden de recorrido
    @Column(nullable = false)
    private Integer orden;
}
