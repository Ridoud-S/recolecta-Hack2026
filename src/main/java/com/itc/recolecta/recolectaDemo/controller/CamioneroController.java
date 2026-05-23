// controller/CamioneroController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.request.EvaluacionRequest;
import com.itc.recolecta.recolectaDemo.dto.request.IncidenciaRequest;
import com.itc.recolecta.recolectaDemo.dto.response.*;
import com.itc.recolecta.recolectaDemo.entity.BadgeOperador;
import com.itc.recolecta.recolectaDemo.entity.Incidencia;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.exception.BusinessException;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.BadgeOperadorRepository;
import com.itc.recolecta.recolectaDemo.repository.IncidenciaRepository;
import com.itc.recolecta.recolectaDemo.repository.RutaRepository;
import com.itc.recolecta.recolectaDemo.repository.UsuarioRepository;
import com.itc.recolecta.recolectaDemo.service.CombustibleService;
import com.itc.recolecta.recolectaDemo.service.GamificacionService;
import com.itc.recolecta.recolectaDemo.service.NotificacionService;
import com.itc.recolecta.recolectaDemo.service.SimuladorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/camionero")
@RequiredArgsConstructor
@Slf4j
public class CamioneroController {

    private final RutaRepository rutaRepository;
    private final UsuarioRepository usuarioRepository;
    private final IncidenciaRepository incidenciaRepository;
    private final BadgeOperadorRepository badgeOperadorRepository;
    private final CombustibleService combustibleService;
    private final GamificacionService gamificacionService;
    private final SimuladorService simuladorService;
    private final NotificacionService notificacionService;

    // ===== GET /api/camionero/mi-ruta → ver mi ruta asignada =====
    @GetMapping("/mi-ruta")
    public ResponseEntity<ApiResponse<RutaStatusResponse>> verMiRuta() {

        Usuario camionero = getUsuarioAutenticado();
        log.info("🚛 Camionero {} consultando su ruta", camionero.getEmail());

        Ruta ruta = rutaRepository.findByCamionero(camionero)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No tienes ninguna ruta asignada"
                ));

        RutaStatusResponse response = combustibleService.obtenerStatusRuta(
                ruta.getRouteId()
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Ruta obtenida", response)
        );
    }

    // ===== POST /api/camionero/ruta/{routeId}/iniciar → iniciar mi ruta =====
    @PostMapping("/ruta/{routeId}/iniciar")
    public ResponseEntity<ApiResponse<String>> iniciarRuta(
            @PathVariable String routeId) {

        Usuario camionero = getUsuarioAutenticado();
        validarRutaPertenece(routeId, camionero);

        log.info("▶️ Camionero {} iniciando ruta {}", camionero.getEmail(), routeId);

        String resultado = simuladorService.avanzarManual(routeId);

        return ResponseEntity.ok(
                ApiResponse.ok("Ruta iniciada exitosamente", resultado)
        );
    }

    // ===== POST /api/camionero/ruta/{routeId}/pausar → pausar ruta =====
    @PostMapping("/ruta/{routeId}/pausar")
    public ResponseEntity<ApiResponse<String>> pausarRuta(
            @PathVariable String routeId) {

        Usuario camionero = getUsuarioAutenticado();
        Ruta ruta = validarRutaPertenece(routeId, camionero);

        log.info("⏸️ Camionero {} pausando ruta {}", camionero.getEmail(), routeId);

        // Cambiar status a PAUSADO
        ruta.setStatus(com.itc.recolecta.recolectaDemo.enums.StatusRuta.PAUSADO);
        rutaRepository.save(ruta);

        return ResponseEntity.ok(
                ApiResponse.ok("Ruta pausada por incidencia", routeId)
        );
    }

    // ===== POST /api/camionero/incidencias → reportar incidencia =====
    @PostMapping("/incidencias")
    public ResponseEntity<ApiResponse<IncidenciaResponse>> reportarIncidencia(
            @Valid @RequestBody IncidenciaRequest request) {

        Usuario camionero = getUsuarioAutenticado();
        log.info("⚠️ Camionero {} reportando incidencia tipo {}",
                camionero.getEmail(), request.getTipo());

        // Validar que la ruta pertenece al camionero
        Ruta ruta = null;
        if (request.getRouteId() != null) {
            ruta = validarRutaPertenece(request.getRouteId(), camionero);
        }

        Incidencia incidencia = Incidencia.builder()
                .reportadoPor(camionero)
                .ruta(ruta)
                .tipo(request.getTipo())
                .descripcion(request.getDescripcion())
                .build();

        incidencia = incidenciaRepository.save(incidencia);

        // Notificar a ciudadanos de la ruta si aplica
        if (ruta != null) {
            notificacionService.notificarIncidencia(ruta, request.getDescripcion());
        }

        IncidenciaResponse response = IncidenciaResponse.builder()
                .id(incidencia.getId())
                .tipo(incidencia.getTipo().name())
                .descripcion(incidencia.getDescripcion())
                .reportadoPor(camionero.getNombre())
                .routeId(ruta != null ? ruta.getRouteId() : null)
                .createdAt(incidencia.getCreatedAt())
                .build();

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Incidencia reportada exitosamente", response));
    }

    // ===== POST /api/camionero/evaluacion/{routeId} → evaluación al finalizar =====
    @PostMapping("/evaluacion/{routeId}")
    public ResponseEntity<ApiResponse<String>> evaluarRuta(
            @PathVariable String routeId,
            @Valid @RequestBody EvaluacionRequest request) {

        Usuario camionero = getUsuarioAutenticado();
        Ruta ruta = validarRutaPertenece(routeId, camionero);

        log.info("📝 Camionero {} evaluando ruta {} — aTiempo={}, incidencia={}",
                camionero.getEmail(), routeId,
                request.getLlegoATiempo(), request.getTuvoIncidencia());

        gamificacionService.procesarEvaluacion(
                camionero, ruta,
                request.getLlegoATiempo(),
                request.getTuvoIncidencia()
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Evaluación registrada. ¡Gracias por tu reporte!")
        );
    }

    // ===== GET /api/camionero/mis-stats → ver mis puntos y badges =====
    @GetMapping("/mis-stats")
    public ResponseEntity<ApiResponse<OperadorStatsResponse>> verMisStats() {

        Usuario camionero = getUsuarioAutenticado();
        log.info("📊 Camionero {} consultando sus stats", camionero.getEmail());

        OperadorStatsResponse response = gamificacionService.obtenerStats(
                camionero.getId()
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Estadísticas obtenidas", response)
        );
    }

    // ===== GET /api/camionero/mis-badges → ver mis badges =====
    @GetMapping("/mis-badges")
    public ResponseEntity<ApiResponse<List<BadgeResponse>>> verMisBadges() {

        Usuario camionero = getUsuarioAutenticado();
        log.info("🏅 Camionero {} consultando sus badges", camionero.getEmail());

        List<BadgeOperador> badges = badgeOperadorRepository
                .findAllByCamioneroOrderByOtorgadoAtDesc(camionero);

        List<BadgeResponse> response = badges.stream()
                .map(b -> BadgeResponse.builder()
                        .id(b.getId())
                        .tipoBadge(b.getTipoBadge().name())
                        .descripcion(b.getDescripcion())
                        .otorgadoAt(b.getOtorgadoAt())
                        .build())
                .toList();

        return ResponseEntity.ok(
                ApiResponse.ok("Badges obtenidos", response)
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

    // ===== HELPER: validar que la ruta pertenece al camionero =====
    private Ruta validarRutaPertenece(String routeId, Usuario camionero) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + routeId
                ));

        if (ruta.getCamionero() == null ||
                !ruta.getCamionero().getId().equals(camionero.getId())) {
            throw new BusinessException(
                    "No tienes permisos para operar la ruta " + routeId
            );
        }

        return ruta;
    }
}
