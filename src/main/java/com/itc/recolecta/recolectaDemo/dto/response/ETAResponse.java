// dto/response/ETAResponse.java
package com.itc.recolecta.recolectaDemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ETAResponse {
    private String routeId;
    private String nombreRuta;
    private String mensaje;
    // Ej: "07:20"
    private String horaEstimadaInicio;
    // Ej: "07:35"
    private String horaEstimadaFin;
    // Ej: 15 (minutos)
    private Integer minutosAproximados;
    private String status;
    private Integer posicionActual;
    private Integer totalPosiciones;
}