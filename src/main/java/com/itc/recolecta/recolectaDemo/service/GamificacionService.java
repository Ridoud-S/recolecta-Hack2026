// service/GamificacionService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.dto.response.OperadorStatsResponse;
import com.itc.recolecta.recolectaDemo.dto.response.RankingOperadorResponse;
import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.enums.TipoBadge;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class GamificacionService {

    private final PuntosOperadorRepository puntosOperadorRepository;
    private final BadgeOperadorRepository badgeOperadorRepository;
    private final EvaluacionRutaRepository evaluacionRutaRepository;
    private final UsuarioRepository usuarioRepository;

    // ===== PUNTOS POR EVENTO =====
    private static final int PUNTOS_RUTA_COMPLETADA = 100;
    private static final int PUNTOS_A_TIEMPO        = 50;
    private static final int PUNTOS_SIN_INCIDENCIAS = 50;
    private static final int PUNTOS_CALIFICACION_5  = 75;
    private static final int PUNTOS_CALIFICACION_4  = 40;
    private static final int PUNTOS_REPORTE_HONESTO = 25;
    private static final int PUNTOS_RUTA_TARDIA     = -30;
    private static final int PUNTOS_SIN_REPORTE     = -50;

    // ===== PROCESAR EVALUACIÓN AL FINALIZAR RUTA =====
    @Transactional
    public void procesarEvaluacion(
            Usuario camionero,
            Ruta ruta,
            Boolean llegoATiempo,
            Boolean tuvoIncidencia) {

        int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        // Obtener o crear registro de puntos del mes
        PuntosOperador puntos = puntosOperadorRepository
                .findByCamioneroAndMesAndAnio(camionero, mes, anio)
                .orElseGet(() -> PuntosOperador.builder()
                        .camionero(camionero)
                        .mes(mes)
                        .anio(anio)
                        .build());

        int puntosGanados = 0;

        // Ruta completada siempre suma
        puntosGanados += PUNTOS_RUTA_COMPLETADA;
        puntos.setRutasCompletadas(puntos.getRutasCompletadas() + 1);

        // Llegó a tiempo
        if (Boolean.TRUE.equals(llegoATiempo)) {
            puntosGanados += PUNTOS_A_TIEMPO;
            puntos.setRutasATiempo(puntos.getRutasATiempo() + 1);
        } else {
            puntosGanados += PUNTOS_RUTA_TARDIA;
        }

        // Sin incidencias
        if (Boolean.FALSE.equals(tuvoIncidencia)) {
            puntosGanados += PUNTOS_SIN_INCIDENCIAS;
        } else {
            // Reporte honesto suma puntos aunque haya incidencia
            puntosGanados += PUNTOS_REPORTE_HONESTO;
            puntos.setIncidenciasReportadas(
                    puntos.getIncidenciasReportadas() + 1
            );
        }

        puntos.setPuntosTotales(puntos.getPuntosTotales() + puntosGanados);
        puntosOperadorRepository.save(puntos);

        log.info("🎮 Camionero {} ganó {} puntos. Total mes: {}",
                camionero.getNombre(), puntosGanados, puntos.getPuntosTotales());

        // Guardar evaluación
        EvaluacionRuta evaluacion = EvaluacionRuta.builder()
                .ruta(ruta)
                .camionero(camionero)
                .llegoATiempo(llegoATiempo)
                .tuvoIncidencia(tuvoIncidencia)
                .build();
        evaluacionRutaRepository.save(evaluacion);

        // Verificar badges
        verificarBadges(camionero, puntos);
    }

    // ===== PROCESAR CALIFICACIÓN DEL CIUDADANO =====
    @Transactional
    public void procesarCalificacion(
            Usuario camionero,
            Ruta ruta,
            Integer calificacion) {

        int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        PuntosOperador puntos = puntosOperadorRepository
                .findByCamioneroAndMesAndAnio(camionero, mes, anio)
                .orElseGet(() -> PuntosOperador.builder()
                        .camionero(camionero)
                        .mes(mes)
                        .anio(anio)
                        .build());

        int puntosCalificacion = 0;
        if (calificacion == 5) puntosCalificacion = PUNTOS_CALIFICACION_5;
        else if (calificacion == 4) puntosCalificacion = PUNTOS_CALIFICACION_4;
        else if (calificacion < 3) puntosCalificacion = -50; // Penalización por mal servicio

        puntos.setPuntosTotales(puntos.getPuntosTotales() + puntosCalificacion);
        puntosOperadorRepository.save(puntos);

        // Actualizar calificación en evaluación
        evaluacionRutaRepository.findByRuta(ruta).ifPresent(eval -> {
            eval.setCalificacionCiudadano(calificacion);
            evaluacionRutaRepository.save(eval);
        });

        log.info("⭐ Calificación {} para camionero {}, +{} puntos",
                calificacion, camionero.getNombre(), puntosCalificacion);
    }

    // ===== VERIFICAR Y OTORGAR BADGES =====
    @Transactional
    protected void verificarBadges(Usuario camionero, PuntosOperador puntos) {

        // Badge: Puntualidad perfecta (5 rutas a tiempo)
        if (puntos.getRutasATiempo() >= 5 &&
                !badgeOperadorRepository.existsByCamioneroAndTipoBadge(
                        camionero, TipoBadge.PUNTUALIDAD_PERFECTA)) {
            otorgarBadge(camionero,
                    TipoBadge.PUNTUALIDAD_PERFECTA,
                    "5 rutas completadas a tiempo este mes");
        }

        // Badge: Sin incidencias (10 rutas sin incidencias)
        int rutasSinIncidencias = puntos.getRutasCompletadas()
                - puntos.getIncidenciasReportadas();
        if (rutasSinIncidencias >= 10 &&
                !badgeOperadorRepository.existsByCamioneroAndTipoBadge(
                        camionero, TipoBadge.SIN_INCIDENCIAS)) {
            otorgarBadge(camionero,
                    TipoBadge.SIN_INCIDENCIAS,
                    "10 rutas completadas sin incidencias");
        }

        // Badge: Recolector del mes (1000+ puntos)
        if (puntos.getPuntosTotales() >= 1000 &&
                !badgeOperadorRepository.existsByCamioneroAndTipoBadge(
                        camionero, TipoBadge.RECOLECTOR_MES)) {
            otorgarBadge(camionero,
                    TipoBadge.RECOLECTOR_MES,
                    "Más de 1000 puntos acumulados este mes 🏆");
        }

        // Badge: Reporte honesto (3+ incidencias reportadas honestamente)
        if (puntos.getIncidenciasReportadas() >= 3 &&
                !badgeOperadorRepository.existsByCamioneroAndTipoBadge(
                        camionero, TipoBadge.REPORTE_HONESTO)) {
            otorgarBadge(camionero,
                    TipoBadge.REPORTE_HONESTO,
                    "Operador honesto: reporta sus incidencias correctamente");
        }
    }

    // ===== OTORGAR BADGE =====
    private void otorgarBadge(
            Usuario camionero,
            TipoBadge tipo,
            String descripcion) {

        BadgeOperador badge = BadgeOperador.builder()
                .camionero(camionero)
                .tipoBadge(tipo)
                .descripcion(descripcion)
                .build();

        badgeOperadorRepository.save(badge);
        log.info("🏅 Badge {} otorgado a {}", tipo, camionero.getNombre());
    }

    // ===== OBTENER STATS DE UN OPERADOR =====
    public OperadorStatsResponse obtenerStats(Long camioneroId) {
        Usuario camionero = usuarioRepository.findById(camioneroId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Camionero no encontrado"
                ));

        int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        PuntosOperador puntos = puntosOperadorRepository
                .findByCamioneroAndMesAndAnio(camionero, mes, anio)
                .orElseGet(() -> PuntosOperador.builder()
                        .camionero(camionero)
                        .puntosTotales(0)
                        .rutasCompletadas(0)
                        .rutasATiempo(0)
                        .incidenciasReportadas(0)
                        .mes(mes)
                        .anio(anio)
                        .build());

        Double promedio = evaluacionRutaRepository
                .findPromedioCalificacion(camionero);

        List<BadgeOperador> badges = badgeOperadorRepository
                .findAllByCamioneroOrderByOtorgadoAtDesc(camionero);

        List<String> nombresBadges = badges.stream()
                .map(b -> b.getTipoBadge().name())
                .toList();

        return OperadorStatsResponse.builder()
                .camioneroId(camionero.getId())
                .nombre(camionero.getNombre())
                .puntosTotales(puntos.getPuntosTotales())
                .rutasCompletadas(puntos.getRutasCompletadas())
                .rutasATiempo(puntos.getRutasATiempo())
                .incidenciasReportadas(puntos.getIncidenciasReportadas())
                .promedioCalificacion(promedio != null ?
                        Math.round(promedio * 10.0) / 10.0 : 0.0)
                .totalBadges(badges.size())
                .badges(nombresBadges)
                .mes(mes)
                .anio(anio)
                .build();
    }

    // ===== RANKING MENSUAL =====
    public List<RankingOperadorResponse> obtenerRanking() {
        int mes = LocalDate.now().getMonthValue();
        int anio = LocalDate.now().getYear();

        List<PuntosOperador> ranking = puntosOperadorRepository
                .findRankingMensual(mes, anio);

        AtomicInteger posicion = new AtomicInteger(1);

        return ranking.stream().map(p -> RankingOperadorResponse.builder()
                .posicion(posicion.getAndIncrement())
                .camioneroId(p.getCamionero().getId())
                .nombre(p.getCamionero().getNombre())
                .puntosTotales(p.getPuntosTotales())
                .rutasCompletadas(p.getRutasCompletadas())
                .rutasATiempo(p.getRutasATiempo())
                .mes(p.getMes())
                .anio(p.getAnio())
                .build()
        ).toList();
    }
}