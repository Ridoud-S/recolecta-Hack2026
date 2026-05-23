package com.itc.recolecta.recolectaDemo.entity;


import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "estado_ruta_actual")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EstadoRutaActual {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "ruta_id", nullable = false)
    private Ruta ruta;

    @Column(name = "position_id_actual")
    @Builder.Default
    private Integer positionIdActual = 1;

    @Column(name = "ultima_actualizacion")
    private LocalDateTime ultimaActualizacion;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private StatusRuta status = StatusRuta.PENDIENTE;
}
