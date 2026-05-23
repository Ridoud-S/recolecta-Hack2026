// dto/response/DomicilioResponse.java
package com.itc.recolecta.recolectaDemo.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DomicilioResponse {
    private Long id;
    private String alias;
    private String calle;
    private String colonia;
    private String codigoPostal;
    private Double lat;
    private Double lng;
    private String zonaCobertura;
    private String routeId;
    private String horarioEstimado;
}