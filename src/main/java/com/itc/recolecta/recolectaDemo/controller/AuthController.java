// controller/AuthController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.request.LoginRequest;
import com.itc.recolecta.recolectaDemo.dto.request.RegisterRequest;
import com.itc.recolecta.recolectaDemo.dto.response.AuthResponse;
import com.itc.recolecta.recolectaDemo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Endpoints para registro, login y gestión de tokens")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar un nuevo usuario", description = "Permite registrar un ciudadano o camionero. Requiere email o teléfono.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario registrado exitosamente", content = @Content(schema = @Schema(implementation = com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (email/teléfono duplicado o faltante)", content = @Content)
    })
    @SecurityRequirements // Sin seguridad para este endpoint
    @PostMapping("/register")
    public ResponseEntity<com.itc.recolecta.recolectaDemo.dto.response.ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.ok("Usuario registrado exitosamente", response));
    }

    @Operation(summary = "Iniciar sesión", description = "Inicia sesión con identificador (email o teléfono) y contraseña.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login exitoso", content = @Content(schema = @Schema(implementation = com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content),
            @ApiResponse(responseCode = "403", description = "Usuario inactivo", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<com.itc.recolecta.recolectaDemo.dto.response.ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.ok("Login exitoso", response)
        );
    }

    @Operation(summary = "Renovar token de acceso", description = "Genera un nuevo token JWT usando un Refresh Token válido.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token renovado exitosamente", content = @Content(schema = @Schema(implementation = com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.class))),
            @ApiResponse(responseCode = "400", description = "Refresh token inválido o expirado", content = @Content)
    })
    @SecurityRequirements
    @PostMapping("/refresh")
    public ResponseEntity<com.itc.recolecta.recolectaDemo.dto.response.ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Refresh-Token") String refreshToken) {

        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(
                com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.ok("Token renovado exitosamente", response)
        );
    }

    @Operation(summary = "Cerrar sesión", description = "Invalida el Refresh Token para cerrar la sesión.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Sesión cerrada exitosamente", content = @Content(schema = @Schema(implementation = com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.class)))
    })
    @SecurityRequirements
    @PostMapping("/logout")
    public ResponseEntity<com.itc.recolecta.recolectaDemo.dto.response.ApiResponse<Void>> logout(
            @RequestHeader("Refresh-Token") String refreshToken) {

        authService.logout(refreshToken);
        return ResponseEntity.ok(
                com.itc.recolecta.recolectaDemo.dto.response.ApiResponse.ok("Sesión cerrada exitosamente")
        );
    }
}