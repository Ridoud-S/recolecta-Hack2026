// dto/request/RegisterRequest.java
package com.itc.recolecta.recolectaDemo.dto.request;

import com.itc.recolecta.recolectaDemo.enums.Rol;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    // Email y teléfono son opcionales individualmente,
    // pero la validación de que al menos uno esté presente
    // se hace en AuthService.register()
    @Email(message = "Email inválido")
    private String email;

    private String telefono;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @NotNull(message = "El rol es obligatorio")
    private Rol rol;
}