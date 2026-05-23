// dto/response/EvaluacionResponse.java
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
public class EvaluacionResponse {
    private Long id;
    private String routeId;
    private Boolean llegoATiempo;
    private Boolean tuvoIncidencia;
    private Integer calificacionCiudadano;
    private LocalDateTime createdAt;
}