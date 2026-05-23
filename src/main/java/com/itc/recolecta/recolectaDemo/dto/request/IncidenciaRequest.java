// dto/request/IncidenciaRequest.java
package com.itc.recolecta.recolectaDemo.dto.request;

import com.itc.recolecta.recolectaDemo.enums.TipoIncidencia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class IncidenciaRequest {

    @NotNull(message = "El tipo de incidencia es obligatorio")
    private TipoIncidencia tipo;

    private String descripcion;

    private String routeId;
}