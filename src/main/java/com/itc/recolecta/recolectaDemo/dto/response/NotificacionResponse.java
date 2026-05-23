// dto/response/NotificacionResponse.java
package com.itc.recolecta.recolectaDemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    private Long id;
    private String tipoEvento;
    private String titulo;
    private String cuerpo;
    private LocalDateTime enviadoAt;
}