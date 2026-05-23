// controller/AdminController.java
package com.itc.recolecta.recolectaDemo.controller;

import com.itc.recolecta.recolectaDemo.dto.response.*;
import com.itc.recolecta.recolectaDemo.entity.Incidencia;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.IncidenciaRepository;
import com.itc.recolecta.recolectaDemo.repository.RutaRepository;
import com.itc.recolecta.recolectaDemo.service.CombustibleService;
import com.itc.recolecta.recolectaDemo.service.GamificacionService;
import com.itc.recolecta.recolectaDemo.service.SimuladorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final SimuladorService simuladorService;
    private final CombustibleService combustibleService;
    private final GamificacionService gamificacionService;
    private final RutaRepository rutaRepository;
    private final IncidenciaRepository incidenciaRepository;

    // ===== GET /api/admin/dashboard → DashboardAdminResponse completo =====
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<DashboardAdminResponse>> getDashboard() {

        log.info("📊 Admin consultando dashboard");

        List<RutaStatusResponse> todasLasRutas = combustibleService.resumenFlotilla();

        long rutasActivas = todasLasRutas.stream()
                .filter(r -> "EN_RUTA".equals(r.getStatus()))
                .count();

        long rutasFinalizadas = todasLasRutas.stream()
                .filter(r -> "FINALIZADO".equals(r.getStatus()))
                .count();

        double kmTotal = todasLasRutas.stream()
                .mapToDouble(r -> r.getKmRecorridos() != null ? r.getKmRecorridos() : 0.0)
                .sum();

        double combustibleTotal = todasLasRutas.stream()
                .mapToDouble(r -> r.getCombustibleConsumido() != null ? r.getCombustibleConsumido() : 0.0)
                .sum();

        // Incidencias de hoy
        LocalDateTime inicioHoy = LocalDate.now().atStartOfDay();
        long incidenciasHoy = incidenciaRepository.findAll().stream()
                .filter(i -> i.getCreatedAt() != null && i.getCreatedAt().isAfter(inicioHoy))
                .count();

        // Rutas activas (en curso)
        List<RutaStatusResponse> rutasEnCurso = todasLasRutas.stream()
                .filter(r -> "EN_RUTA".equals(r.getStatus()) || "PENDIENTE".equals(r.getStatus()))
                .toList();

        // Top 5 operadores
        List<RankingOperadorResponse> ranking = gamificacionService.obtenerRanking();
        List<RankingOperadorResponse> topOperadores = ranking.stream()
                .limit(5)
                .toList();

        DashboardAdminResponse response = DashboardAdminResponse.builder()
                .totalRutasActivas((int) rutasActivas)
                .totalRutasFinalizadas((int) rutasFinalizadas)
                .totalIncidenciasHoy((int) incidenciasHoy)
                .kmTotalRecorridos(Math.round(kmTotal * 100.0) / 100.0)
                .combustibleTotalConsumido(Math.round(combustibleTotal * 100.0) / 100.0)
                .rutasEnCurso(rutasEnCurso)
                .topOperadores(topOperadores)
                .build();

        return ResponseEntity.ok(
                ApiResponse.ok("Dashboard obtenido", response)
        );
    }

    // ===== GET /api/admin/rutas → listar todas las rutas con status =====
    @GetMapping("/rutas")
    public ResponseEntity<ApiResponse<List<RutaStatusResponse>>> listarRutas() {

        log.info("📋 Admin listando todas las rutas");
        List<RutaStatusResponse> response = combustibleService.resumenFlotilla();
        return ResponseEntity.ok(
                ApiResponse.ok("Rutas obtenidas", response)
        );
    }

    // ===== GET /api/admin/rutas/{routeId}/status → status detallado =====
    @GetMapping("/rutas/{routeId}/status")
    public ResponseEntity<ApiResponse<RutaStatusResponse>> statusRuta(
            @PathVariable String routeId) {

        log.info("🔍 Admin consultando status de ruta {}", routeId);
        RutaStatusResponse response = combustibleService.obtenerStatusRuta(routeId);
        return ResponseEntity.ok(
                ApiResponse.ok("Status de ruta obtenido", response)
        );
    }

    // ===== GET /api/admin/rutas/{routeId}/combustible → km, litros y costo =====
    @GetMapping("/rutas/{routeId}/combustible")
    public ResponseEntity<ApiResponse<Map<String, Object>>> combustibleRuta(
            @PathVariable String routeId) {

        log.info("⛽ Admin consultando combustible de ruta {}", routeId);

        double km = combustibleService.calcularKmRecorridos(routeId);
        double litros = combustibleService.calcularCombustible(routeId);
        double costo = combustibleService.calcularCosto(routeId);

        Map<String, Object> response = Map.of(
                "routeId", routeId,
                "kmRecorridos", km,
                "litrosConsumidos", litros,
                "costoEstimado", costo
        );

        return ResponseEntity.ok(
                ApiResponse.ok("Datos de combustible obtenidos", response)
        );
    }

    // ===== GET /api/admin/operadores/ranking → ranking mensual =====
    @GetMapping("/operadores/ranking")
    public ResponseEntity<ApiResponse<List<RankingOperadorResponse>>> rankingOperadores() {

        log.info("🏆 Admin consultando ranking de operadores");
        List<RankingOperadorResponse> response = gamificacionService.obtenerRanking();
        return ResponseEntity.ok(
                ApiResponse.ok("Ranking obtenido", response)
        );
    }

    // ===== GET /api/admin/operadores/{id}/stats → stats de un camionero =====
    @GetMapping("/operadores/{id}/stats")
    public ResponseEntity<ApiResponse<OperadorStatsResponse>> statsOperador(
            @PathVariable Long id) {

        log.info("📊 Admin consultando stats del operador id={}", id);
        OperadorStatsResponse response = gamificacionService.obtenerStats(id);
        return ResponseEntity.ok(
                ApiResponse.ok("Stats del operador obtenidas", response)
        );
    }

    // ===== GET /api/admin/incidencias → todas las incidencias =====
    @GetMapping("/incidencias")
    public ResponseEntity<ApiResponse<List<IncidenciaResponse>>> listarIncidencias() {

        log.info("⚠️ Admin listando todas las incidencias");

        List<Incidencia> incidencias = incidenciaRepository.findAll();

        List<IncidenciaResponse> response = incidencias.stream()
                .map(this::mapToIncidenciaResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.ok("Incidencias obtenidas", response)
        );
    }

    // ===== GET /api/admin/incidencias/ruta/{routeId} → incidencias de una ruta =====
    @GetMapping("/incidencias/ruta/{routeId}")
    public ResponseEntity<ApiResponse<List<IncidenciaResponse>>> incidenciasPorRuta(
            @PathVariable String routeId) {

        log.info("⚠️ Admin consultando incidencias de ruta {}", routeId);

        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + routeId
                ));

        List<Incidencia> incidencias = incidenciaRepository
                .findAllByRutaOrderByCreatedAtDesc(ruta);

        List<IncidenciaResponse> response = incidencias.stream()
                .map(this::mapToIncidenciaResponse)
                .toList();

        return ResponseEntity.ok(
                ApiResponse.ok("Incidencias de ruta obtenidas", response)
        );
    }

    // ===== POST /api/admin/demo/ruta/{routeId}/avanzar → avanzar ruta =====
    @PostMapping("/demo/ruta/{routeId}/avanzar")
    public ResponseEntity<ApiResponse<String>> avanzarRuta(
            @PathVariable String routeId) {

        log.info("⏩ Admin avanzando ruta {} manualmente", routeId);
        String resultado = simuladorService.avanzarManual(routeId);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    // ===== POST /api/admin/demo/ruta/{routeId}/reiniciar → reiniciar ruta =====
    @PostMapping("/demo/ruta/{routeId}/reiniciar")
    public ResponseEntity<ApiResponse<String>> reiniciarRuta(
            @PathVariable String routeId) {

        log.info("🔄 Admin reiniciando ruta {} para demo", routeId);
        String resultado = simuladorService.reiniciarRuta(routeId);
        return ResponseEntity.ok(ApiResponse.ok(resultado));
    }

    // ===== HELPER: mapear Incidencia a IncidenciaResponse =====
    private IncidenciaResponse mapToIncidenciaResponse(Incidencia incidencia) {
        return IncidenciaResponse.builder()
                .id(incidencia.getId())
                .tipo(incidencia.getTipo().name())
                .descripcion(incidencia.getDescripcion())
                .reportadoPor(incidencia.getReportadoPor() != null
                        ? incidencia.getReportadoPor().getNombre()
                        : "Desconocido")
                .routeId(incidencia.getRuta() != null
                        ? incidencia.getRuta().getRouteId()
                        : null)
                .createdAt(incidencia.getCreatedAt())
                .build();
    }
}