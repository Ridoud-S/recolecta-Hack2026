// dto/request/LoginRequest.java
package com.itc.recolecta.recolectaDemo.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    /**
     * Puede ser un email (ej: "juan@gmail.com") o un teléfono (ej: "5551234567").
     * El servicio detecta automáticamente cuál es.
     */
    @NotBlank(message = "El email o teléfono es obligatorio")
    private String identifier;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}