// controller/CiudadanoController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.request.DomicilioRequest;
import com.itc.recolecta.recolectaDemo.dto.response.*;
import com.itc.recolecta.recolectaDemo.entity.NotificacionLog;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.exception.BusinessException;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.RutaRepository;
import com.itc.recolecta.recolectaDemo.repository.UsuarioRepository;
import com.itc.recolecta.recolectaDemo.service.DomicilioService;
import com.itc.recolecta.recolectaDemo.service.ETAService;
import com.itc.recolecta.recolectaDemo.service.GamificacionService;
import com.itc.recolecta.recolectaDemo.service.NotificacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ciudadano")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Ciudadano", description = "Endpoints para los usuarios ciudadanos")
@SecurityRequirement(name = "bearerAuth")
public class CiudadanoController {

    private final DomicilioService domicilioService;
    private final ETAService etaService;
    private final NotificacionService notificacionService;
    private final GamificacionService gamificacionService;
    private final UsuarioRepository usuarioRepository;
    private final RutaRepository rutaRepository;

    // ===== POST /api/ciudadano/domicilios → registrar domicilio =====
    @Operation(summary = "Registrar domicilio", description = "Añade una nueva dirección o punto de recolección para el ciudadano.")
    @PostMapping("/domicilios")
    public ResponseEntity<ApiResponse<DomicilioResponse>> registrarDomicilio(
            @Valid @RequestBody DomicilioRequest request) {

        log.info("📍 Registrando nuevo domicilio para usuario autenticado");
        DomicilioResponse response = domicilioService.registrar(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Domicilio registrado exitosamente", response));
    }

    // ===== GET /api/ciudadano/domicilios → listar mis domicilios =====
    @Operation(summary = "Listar domicilios", description = "Lista todos los domicilios registrados por el ciudadano.")
    @GetMapping("/domicilios")
    public ResponseEntity<ApiResponse<List<DomicilioResponse>>> listarDomicilios() {

        log.info("📋 Listando domicilios del usuario autenticado");
        List<DomicilioResponse> response = domicilioService.listarMios();
        return ResponseEntity.ok(
                ApiResponse.ok("Domicilios obtenidos", response)
        );
    }

    // ===== GET /api/ciudadano/domicilios/{id} → obtener domicilio por id =====
    @Operation(summary = "Obtener domicilio por ID", description = "Obtiene los detalles de un domicilio específico del ciudadano.")
    @GetMapping("/domicilios/{id}")
    public ResponseEntity<ApiResponse<DomicilioResponse>> obtenerDomicilio(
            @PathVariable Long id) {

        log.info("🔍 Obteniendo domicilio id={}", id);
        DomicilioResponse response = domicilioService.obtenerPorId(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Domicilio obtenido", response)
        );
    }

    // ===== DELETE /api/ciudadano/domicilios/{id} → eliminar domicilio =====
    @Operation(summary = "Eliminar domicilio", description = "Elimina un domicilio registrado por el ciudadano.")
    @DeleteMapping("/domicilios/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarDomicilio(
            @PathVariable Long id) {

        log.info("🗑️ Eliminando domicilio id={}", id);
        domicilioService.eliminar(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Domicilio eliminado exitosamente")
        );
    }

    // ===== GET /api/ciudadano/eta/{domicilioId} → obtener ETA =====
    @Operation(summary = "Obtener ETA", description = "Calcula el Tiempo Estimado de Llegada del camión al domicilio proporcionado.")
    @GetMapping("/eta/{domicilioId}")
    public ResponseEntity<ApiResponse<ETAResponse>> obtenerETA(
            @PathVariable Long domicilioId) {

        log.info("⏱️ Consultando ETA para domicilio id={}", domicilioId);
        ETAResponse response = etaService.obtenerETA(domicilioId);
        return ResponseEntity.ok(
                ApiResponse.ok("ETA obtenido", response)
        );
    }

    // ===== GET /api/ciudadano/notificaciones → historial últimas 10 =====
    @Operation(summary = "Historial de notificaciones", description = "Muestra las últimas notificaciones enviadas al ciudadano.")
    @GetMapping("/notificaciones")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> obtenerNotificaciones() {

        Usuario usuario = getUsuarioAutenticado();
        log.info("🔔 Consultando notificaciones de {}", usuario.getEmail());

        List<NotificacionLog> historial = notificacionService.obtenerHistorial(usuario);

        List<NotificacionResponse> response = historial.stream()
                .map(n -> NotificacionResponse.builder()
                        .id(n.getId())
                        .tipoEvento(n.getTipoEvento())
                        .titulo(n.getTitulo())
                        .cuerpo(n.getCuerpo())
                        .enviadoAt(n.getEnviadoAt())
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.ok("Notificaciones obtenidas", response)
        );
    }

    // ===== POST /api/ciudadano/calificacion/{rutaId} → calificar servicio =====
    @Operation(summary = "Calificar servicio", description = "Permite al ciudadano calificar el servicio de recolección en una ruta (1 a 5 estrellas).")
    @PostMapping("/calificacion/{rutaId}")
    public ResponseEntity<ApiResponse<String>> calificarServicio(
            @PathVariable String rutaId,
            @RequestBody Map<String, Integer> body) {

        Integer calificacion = body.get("calificacion");

        if (calificacion == null || calificacion < 1 || calificacion > 5) {
            throw new BusinessException(
                    "La calificación debe ser entre 1 y 5 estrellas"
            );
        }

        Ruta ruta = rutaRepository.findByRouteId(rutaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + rutaId
                ));

        if (ruta.getCamionero() == null) {
            throw new BusinessException(
                    "Esta ruta no tiene operador asignado"
            );
        }

        log.info("⭐ Calificación {} estrellas para ruta {} por {}",
                calificacion, rutaId, getUsuarioAutenticado().getEmail());

        gamificacionService.procesarCalificacion(
                ruta.getCamionero(), ruta, calificacion
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Calificación registrada exitosamente. ¡Gracias!")
        );
    }

    // ===== HELPER: obtener usuario autenticado =====
    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"
                ));
    }
}