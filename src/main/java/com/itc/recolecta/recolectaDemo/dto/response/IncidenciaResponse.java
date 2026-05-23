// dto/response/IncidenciaResponse.java
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
public class IncidenciaResponse {
    private Long id;
    private String tipo;
    private String descripcion;
    private String reportadoPor;
    private String routeId;
    private LocalDateTime createdAt;
}