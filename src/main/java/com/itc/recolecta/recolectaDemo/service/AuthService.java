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

        // Validar que venga al menos email o teléfono
        boolean tieneEmail    = request.getEmail() != null && !request.getEmail().isBlank();
        boolean tieneTelefono = request.getTelefono() != null && !request.getTelefono().isBlank();

        if (!tieneEmail && !tieneTelefono) {
            throw new BusinessException("Debes proporcionar un email o un teléfono para registrarte");
        }

        // Validar duplicados
        if (tieneEmail && usuarioRepository.existsByEmail(request.getEmail())) {
            throw new BusinessException("El email ya está registrado");
        }
        if (tieneTelefono && usuarioRepository.existsByTelefono(request.getTelefono())) {
            throw new BusinessException("El teléfono ya está registrado");
        }

        // Determinar el identificador principal (usado como subject en el JWT)
        // Preferimos email cuando está disponible; si no, usamos el teléfono.
        String principalIdentifier = tieneEmail ? request.getEmail() : request.getTelefono();

        // Crear usuario
        Usuario usuario = Usuario.builder()
                .nombre(request.getNombre())
                .email(tieneEmail ? request.getEmail() : null)
                .telefono(tieneTelefono ? request.getTelefono() : null)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .rol(request.getRol())
                .build();

        usuarioRepository.save(usuario);

        // Generar tokens
        String accessToken  = jwtUtil.generateToken(principalIdentifier, usuario.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(principalIdentifier);

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

        // Resolver el usuario por email o teléfono
        String identifier = request.getIdentifier().trim();
        Usuario usuario   = resolveUsuario(identifier);

        // Validar estado
        if (!usuario.getActivo()) {
            throw new BusinessException("Usuario inactivo, contacta al administrador");
        }

        // Validar contraseña manualmente (Spring Security usa email como username;
        // cuando el usuario se registró solo con teléfono el AuthenticationManager
        // no lo encontraría, así que validamos directo contra el hash).
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            throw new BusinessException("Credenciales inválidas");
        }

        // Determinar el identificador principal para el JWT
        String principalIdentifier = usuario.getEmail() != null ? usuario.getEmail() : usuario.getTelefono();

        // Generar tokens
        String accessToken  = jwtUtil.generateToken(principalIdentifier, usuario.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(principalIdentifier);

        // Rotar refresh token
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
        String principalIdentifier = usuario.getEmail() != null ? usuario.getEmail() : usuario.getTelefono();

        // Generar nuevo access token
        String newAccessToken  = jwtUtil.generateToken(principalIdentifier, usuario.getRol().name());
        String newRefreshToken = jwtUtil.generateRefreshToken(principalIdentifier);

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

    // ===== HELPERS =====

    /**
     * Detecta si el identificador es un email (contiene '@') o un teléfono,
     * y busca el usuario correspondiente en la base de datos.
     */
    private Usuario resolveUsuario(String identifier) {
        if (identifier.contains("@")) {
            return usuarioRepository.findByEmail(identifier)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        } else {
            return usuarioRepository.findByTelefono(identifier)
                    .orElseThrow(() -> new BusinessException("Usuario no encontrado"));
        }
    }

    private void saveRefreshToken(Usuario usuario, String token) {
        RefreshToken refreshToken = RefreshToken.builder()
                .usuario(usuario)
                .token(token)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();
        refreshTokenRepository.save(refreshToken);
    }
}