// dto/response/RutaStatusResponse.java
package com.itc.recolecta.recolectaDemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RutaStatusResponse {
    private String routeId;
    private String nombre;
    private String status;
    private Integer posicionActual;
    private Integer totalPosiciones;
    private String ultimaActualizacion;
    // Para el dashboard del admin
    private Double kmRecorridos;
    private Double combustibleConsumido;
}