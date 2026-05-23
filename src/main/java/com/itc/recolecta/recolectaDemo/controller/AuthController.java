// controller/AuthController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.request.LoginRequest;
import com.itc.recolecta.recolectaDemo.dto.request.RegisterRequest;
import com.itc.recolecta.recolectaDemo.dto.response.ApiResponse;
import com.itc.recolecta.recolectaDemo.dto.response.AuthResponse;
import com.itc.recolecta.recolectaDemo.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // POST /api/auth/register
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Usuario registrado exitosamente", response));
    }

    // POST /api/auth/login
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request) {

        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(
                ApiResponse.ok("Login exitoso", response)
        );
    }

    // POST /api/auth/refresh
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            @RequestHeader("Refresh-Token") String refreshToken) {

        AuthResponse response = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.ok("Token renovado exitosamente", response)
        );
    }

    // POST /api/auth/logout
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader("Refresh-Token") String refreshToken) {

        authService.logout(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.ok("Sesión cerrada exitosamente")
        );
    }
}