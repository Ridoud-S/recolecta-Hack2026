package com.itc.recolecta.recolectaDemo.entity;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    // ROUTE_START, TRUCK_PROXIMITY, ROUTE_COMPLETED
    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String cuerpo;

    @Column(name = "enviado_at")
    @Builder.Default
    private LocalDateTime enviadoAt = LocalDateTime.now();
}
