// service/CombustibleService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.dto.response.RutaStatusResponse;
import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CombustibleService {

    private final RutaRepository rutaRepository;
    private final CamionRepository camionRepository;
    private final PosicionRutaRepository posicionRutaRepository;
    private final EstadoRutaRepository estadoRutaRepository;
    private final GeocodificacionService geocodificacionService;

    // Precio aproximado combustible MX
    private static final double PRECIO_LITRO = 24.50;

    // ===== CALCULAR KM RECORRIDOS DE UNA RUTA =====
    public double calcularKmRecorridos(String routeId) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + routeId
                ));

        List<PosicionRuta> posiciones = posicionRutaRepository
                .findAllByRutaOrderByOrdenAsc(ruta);

        if (posiciones.size() < 2) return 0.0;

        double totalKm = 0.0;

        for (int i = 0; i < posiciones.size() - 1; i++) {
            PosicionRuta actual = posiciones.get(i);
            PosicionRuta siguiente = posiciones.get(i + 1);

            totalKm += geocodificacionService.calcularDistanciaKm(
                    actual.getLat(), actual.getLng(),
                    siguiente.getLat(), siguiente.getLng()
            );
        }

        return Math.round(totalKm * 100.0) / 100.0;
    }

    // ===== CALCULAR COMBUSTIBLE CONSUMIDO =====
    public double calcularCombustible(String routeId) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + routeId
                ));

        if (ruta.getCamion() == null) {
            log.warn("Ruta {} sin camión asignado", routeId);
            return 0.0;
        }

        double kmRecorridos = calcularKmRecorridos(routeId);
        double rendimiento = ruta.getCamion().getRendimientoKmLitro();

        if (rendimiento == 0) return 0.0;

        double litrosConsumidos = kmRecorridos / rendimiento;
        return Math.round(litrosConsumidos * 100.0) / 100.0;
    }

    // ===== CALCULAR COSTO EN PESOS =====
    public double calcularCosto(String routeId) {
        double litros = calcularCombustible(routeId);
        return Math.round(litros * PRECIO_LITRO * 100.0) / 100.0;
    }

    // ===== STATUS COMPLETO DE RUTA PARA ADMIN =====
    public RutaStatusResponse obtenerStatusRuta(String routeId) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + routeId
                ));

        EstadoRutaActual estado = estadoRutaRepository
                .findByRuta(ruta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Estado de ruta no encontrado"
                ));

        int totalPosiciones = posicionRutaRepository.countByRuta(ruta);
        double kmRecorridos = calcularKmRecorridos(routeId);
        double combustible = calcularCombustible(routeId);

        return RutaStatusResponse.builder()
                .routeId(ruta.getRouteId())
                .nombre(ruta.getNombre())
                .status(estado.getStatus().name())
                .posicionActual(estado.getPositionIdActual())
                .totalPosiciones(totalPosiciones)
                .ultimaActualizacion(estado.getUltimaActualizacion() != null
                        ? estado.getUltimaActualizacion().toString()
                        : "Sin datos")
                .kmRecorridos(kmRecorridos)
                .combustibleConsumido(combustible)
                .build();
    }

    // ===== RESUMEN FLOTILLA COMPLETA PARA DASHBOARD =====
    public List<RutaStatusResponse> resumenFlotilla() {
        return rutaRepository.findAll()
                .stream()
                .map(ruta -> obtenerStatusRuta(ruta.getRouteId()))
                .toList();
    }

    // ===== VERIFICAR DISCREPANCIA (PREVENCIÓN DE FRAUDE) =====
    public void verificarDiscrepanciaCombustible(String routeId, double litrosSolicitados) {
        Ruta ruta = rutaRepository.findByRouteId(routeId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ruta no encontrada: " + routeId
                ));

        double litrosTeoricos = calcularCombustible(routeId);
        
        // Tolerancia del 15% (por tráfico, paradas, etc.)
        double maxTolerancia = litrosTeoricos * 1.15;

        if (litrosSolicitados > maxTolerancia) {
            log.warn("🚨 ALERTA DE FRAUDE: Ruta {} solicitó {}L, pero el teórico es {}L (Max permitido {}L)", 
                    routeId, litrosSolicitados, litrosTeoricos, maxTolerancia);
            ruta.setFraudeSospechoso(true);
            rutaRepository.save(ruta);
        } else {
            log.info("✅ Combustible validado para ruta {}: {}L solicitados, {}L teóricos", 
                    routeId, litrosSolicitados, litrosTeoricos);
            ruta.setFraudeSospechoso(false);
            rutaRepository.save(ruta);
        }
    }
}