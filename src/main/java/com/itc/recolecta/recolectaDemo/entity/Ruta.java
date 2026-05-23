package com.itc.recolecta.recolectaDemo.entity;


import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "rutas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ruta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Ej: "RUTA-01"
    @Column(name = "route_id", unique = true, nullable = false)
    private String routeId;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusRuta status = StatusRuta.PENDIENTE;

    @OneToOne
    @JoinColumn(name = "camion_id")
    private Camion camion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camionero_id")
    private Usuario camionero;

    @Column(name = "es_nocturna")
    @Builder.Default
    private Boolean esNocturna = false;

    @Column(name = "fraude_sospechoso")
    @Builder.Default
    private Boolean fraudeSospechoso = false;
}
