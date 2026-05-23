// service/SimuladorService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.dto.response.ETAResponse;
import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@EnableScheduling
public class SimuladorService {

    private final EstadoRutaRepository estadoRutaRepository;
    private final PosicionRutaRepository posicionRutaRepository;
    private final RutaRepository rutaRepository;
    private final NotificacionService notificacionService;
    private final SimpMessagingTemplate messagingTemplate;

    // ===== SCHEDULER: Avanza posición cada 30 segundos =====
    @Scheduled(fixedRateString = "${app.simulation.interval-ms:30000}")
    @Transactional
    @CacheEvict(value = "etas", allEntries = true)
    public void avanzarRutas() {
        // Solo rutas activas
        List<EstadoRutaActual> rutasActivas = estadoRutaRepository
                .findAllByStatusIn(List.of(
                        StatusRuta.EN_RUTA,
                        StatusRuta.PENDIENTE
                ));

        if (rutasActivas.isEmpty()) {
            log.debug("No hay rutas activas en este momento");
            return;
        }

        for (EstadoRutaActual estado : rutasActivas) {
            procesarAvance(estado);
        }
    }

    // ===== CRON JOB: DETECTAR RUTAS ABANDONADAS (> 12 hrs) =====
    @Scheduled(fixedRate = 3600000) // Se ejecuta cada hora
    @Transactional
    public void monitorearRutasAbandonadas() {
        LocalDateTime limite = LocalDateTime.now().minusHours(12);
        List<EstadoRutaActual> rutasAbandonadas = estadoRutaRepository
                .findAllByStatusIn(List.of(StatusRuta.EN_RUTA)).stream()
                .filter(estado -> estado.getUltimaActualizacion() != null && 
                                  estado.getUltimaActualizacion().isBefore(limite))
                .toList();

        for (EstadoRutaActual estado : rutasAbandonadas) {
            log.warn("⚠️ Ruta {} marcada como ANOMALIA (más de 12 horas sin finalizar)", estado.getRuta().getRouteId());
            estado.setStatus(StatusRuta.ANOMALIA);
            estadoRutaRepository.save(estado);

            Ruta ruta = estado.getRuta();
            ruta.setStatus(StatusRuta.ANOMALIA);
            rutaRepository.save(ruta);
        }
    }

    // ===== PROCESAR AVANCE DE UNA RUTA =====
    @Transactional
    private void procesarAvance(EstadoRutaActual estado) {
        Ruta ruta = estado.getRuta();
        int posicionActual = estado.getPositionIdActual();
        int totalPosiciones = posicionRutaRepository.countByRuta(ruta);

        // Activar ruta si está pendiente
        if (estado.getStatus() == StatusRuta.PENDIENTE) {
            estado.setStatus(StatusRuta.EN_RUTA);
            estadoRutaRepository.save(estado);
            notificacionService.notificarInicioRuta(ruta);
            ruta.setStatus(StatusRuta.EN_RUTA);
            rutaRepository.save(ruta);
            log.info("🚛 Ruta {} iniciada", ruta.getRouteId());
            return;
        }

        // Avanzar a siguiente posición
        int siguientePosicion = posicionActual + 1;

        // Verificar si llegó al punto de proximidad (posición 4)
        if (siguientePosicion == 4) {
            notificacionService.notificarCamionCercano(ruta);
            log.info("📍 Ruta {} cerca del destino", ruta.getRouteId());
        }

        // Verificar si completó la ruta (posición 8 = regreso al basurero)
        if (siguientePosicion >= totalPosiciones) {
            estado.setStatus(StatusRuta.FINALIZADO);
            estado.setPositionIdActual(totalPosiciones);
            estado.setUltimaActualizacion(LocalDateTime.now());
            estadoRutaRepository.save(estado);

            ruta.setStatus(StatusRuta.FINALIZADO);
            rutaRepository.save(ruta);

            notificacionService.notificarRutaCompletada(ruta);
            log.info("✅ Ruta {} finalizada", ruta.getRouteId());

            // Broadcast WebSocket de finalización
            broadcastEstado(estado, ruta, totalPosiciones);
            return;
        }

        // Actualizar posición
        estado.setPositionIdActual(siguientePosicion);
        estado.setUltimaActualizacion(LocalDateTime.now());
        estadoRutaRepository.save(estado);

        log.info("🚛 Ruta {} avanzó a posición {}/{}",
                ruta.getRouteId(), siguientePosicion, totalPosiciones);

        // Broadcast WebSocket a ciudadanos
        broadcastEstado(estado, ruta, totalPosiciones);
    }

    // ===== BROADCAST WEBSOCKET =====
    private void broadcastEstado(
            EstadoRutaActual estado,
            Ruta ruta,
            int totalPosiciones) {

        ETAResponse etaUpdate = ETAResponse.builder()
                .routeId(ruta.getRouteId())
                .nombreRuta(ruta.getNombre())
                .status(estado.getStatus().name())
                .posicionActual(estado.getPositionIdActual())
                .totalPosiciones(totalPosiciones)
                .mensaje(getMensajeEstado(estado.getStatus()))
                .build();

        // Cada ruta tiene su propio canal
        // El ciudadano solo se suscribe al canal de su ruta
        messagingTemplate.convertAndSend(
                "/topic/ruta/" + ruta.getRouteId(),
                etaUpdate
        );

        log.debug("📡 WebSocket broadcast → /topic/ruta/{}",
                ruta.getRouteId());
    }

    // ===== ENDPOINT MANUAL PARA DEMO =====
    @Transactional
    @CacheEvict(value = "etas", allEntries = true)
    public String avanzarManual(String routeId) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new RuntimeException(
                        "Ruta no encontrada: " + routeId
                ));

        EstadoRutaActual estado = estadoRutaRepository
                .findByRuta(ruta)
                .orElseThrow(() -> new RuntimeException(
                        "Estado no encontrado para ruta: " + routeId
                ));

        procesarAvance(estado);

        return "Ruta " + routeId + " avanzó a posición "
                + estado.getPositionIdActual();
    }

    // ===== REINICIAR RUTA PARA DEMO =====
    @Transactional
    @CacheEvict(value = "etas", allEntries = true)
    public String reiniciarRuta(String routeId) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new RuntimeException(
                        "Ruta no encontrada: " + routeId
                ));

        EstadoRutaActual estado = estadoRutaRepository
                .findByRuta(ruta)
                .orElseThrow(() -> new RuntimeException(
                        "Estado no encontrado para ruta: " + routeId
                ));

        estado.setPositionIdActual(1);
        estado.setStatus(StatusRuta.PENDIENTE);
        estado.setUltimaActualizacion(LocalDateTime.now());
        estadoRutaRepository.save(estado);

        ruta.setStatus(StatusRuta.PENDIENTE);
        rutaRepository.save(ruta);

        log.info("🔄 Ruta {} reiniciada para demo", routeId);
        return "Ruta " + routeId + " reiniciada";
    }

    // ===== HELPER =====
    private String getMensajeEstado(StatusRuta status) {
        return switch (status) {
            case PENDIENTE -> "El camión aún no ha salido";
            case EN_RUTA -> "El camión está en camino";
            case FINALIZADO -> "El servicio ha finalizado por hoy";
            case PAUSADO -> "El camión está pausado temporalmente";
            case CANCELADO -> "El servicio fue cancelado";
            case ANOMALIA -> "El servicio presenta una anomalía o fue abandonado";
        };
    }
}