// service/AuthService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.dto.request.LoginRequest;
import com.itc.recolecta.recolectaDemo.dto.request.RegisterRequest;
import com.itc.recolecta.recolectaDemo.dto.response.AuthResponse;
import com.itc.recolecta.recolectaDemo.entity.RefreshToken;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.exception.BusinessException;
import com.itc.recolecta.recolectaDemo.repository.RefreshTokenRepository;
import com.itc.recolecta.recolectaDemo.repository.UsuarioRepository;
import com.itc.recolecta.recolectaDemo.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    // ===== REGISTRO =====
    @Transactional
    public AuthResponse register(RegisterRequest request) {

        // Validar email duplicado
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }

        // Validar teléfono duplicado si viene
        if (request.getTelefono() != null &&
                usuarioRepository.existsByTelefono(request.getTelefono())) {
            throw new BusinessException("El teléfono ya está registrado");
        }

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(request.getEmail())
                .telefono(request.getTelefono())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .build();

        usuarioRepository.save(usuario);

        // Generar tokens
        String accessToken = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        // Guardar refresh token
        saveRefreshToken(usuario, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .rol(usuario.getRol().name())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
    }

    // ===== LOGIN =====
    @Transactional
    public AuthResponse login(LoginRequest request) {

        // Spring Security valida credenciales
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException("Usuario no encontrado"));

        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario inactivo, contacta al administrador");
        }

        // Generar tokens
        String accessToken = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().name()
        );
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        // Eliminar refresh tokens anteriores y guardar nuevo
        refreshTokenRepository.deleteByUsuario(usuario);
        saveRefreshToken(usuario, refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .rol(usuario.getRol().name())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
    }

    // ===== REFRESH TOKEN =====
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {

        // Validar que el token existe en DB
        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new BusinessException("Refresh token inválido"));

        // Validar expiración
        if (storedToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(storedToken);
            throw new BusinessException("Refresh token expirado, inicia sesión nuevamente");
        }

        // Validar firma JWT
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException("Refresh token inválido");
        }

        Usuario usuario = storedToken.getUsuario();

        // Generar nuevo access token
        String newAccessToken = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().name()
        );
        String newRefreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        // Rotar refresh token
        refreshTokenRepository.delete(storedToken);
        saveRefreshToken(usuario, newRefreshToken);

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .rol(usuario.getRol().name())
                .nombre(usuario.getNombre())
                .email(usuario.getEmail())
                .build();
    }

    // ===== LOGOUT =====
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    // ===== HELPER =====
    private void saveRefreshToken(Usuario usuario, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}