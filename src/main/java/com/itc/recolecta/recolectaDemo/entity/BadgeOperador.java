package com.itc.recolecta.recolectaDemo.entity;

import com.itc.recolecta.recolectaDemo.enums.TipoBadge;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "badges_operador")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BadgeOperador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camionero_id", nullable = false)
    private Usuario camionero;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_badge", nullable = false)
    private TipoBadge tipoBadge;

    @Column(length = 200)
    private String descripcion;

    @Column(name = "otorgado_at")
    @Builder.Default
    private LocalDateTime otorgadoAt = LocalDateTime.now();
}
