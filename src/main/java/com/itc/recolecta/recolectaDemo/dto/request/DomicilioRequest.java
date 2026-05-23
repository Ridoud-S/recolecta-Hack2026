// dto/request/DomicilioRequest.java
package com.itc.recolecta.recolectaDemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DomicilioRequest {

    @NotBlank(message = "El alias es obligatorio")
    private String alias;

    @NotBlank(message = "La calle es obligatoria")
    private String calle;

    @NotBlank(message = "La colonia es obligatoria")
    private String colonia;

    private String codigoPostal;
}