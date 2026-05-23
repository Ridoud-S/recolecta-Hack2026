// dto/request/EvaluacionRequest.java
package com.itc.recolecta.recolectaDemo.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EvaluacionRequest {

    @NotNull(message = "Indica si llegaste a tiempo")
    private Boolean llegoATiempo;

    @NotNull(message = "Indica si tuviste incidencia")
    private Boolean tuvoIncidencia;
}