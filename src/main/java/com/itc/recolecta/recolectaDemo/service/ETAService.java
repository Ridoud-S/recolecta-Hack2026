// service/ETAService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.dto.response.ETAResponse;
import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import com.itc.recolecta.recolectaDemo.exception.BusinessException;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class ETAService {

    private final DomicilioRepository domicilioRepository;
    private final UsuarioRepository usuarioRepository;
    private final EstadoRutaRepository estadoRutaRepository;
    private final PosicionRutaRepository posicionRutaRepository;
    private final GeocodificacionService geocodificacionService;

    private static final DateTimeFormatter TIME_FORMAT =
            DateTimeFormatter.ofPattern("HH:mm");

    // ===== OBTENER ETA DE UN DOMICILIO =====
    public ETAResponse obtenerETA(Long domicilioId) {
        Usuario usuario = getUsuarioAutenticado();

        // RBAC: validar que el domicilio pertenece al usuario
        Domicilio domicilio = domicilioRepository
                .findByIdAndUsuario(domicilioId, usuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Domicilio no encontrado o no te pertenece"
                ));

        // Validar que tiene zona asignada
        if (domicilio.getZonaCobertura() == null) {
            throw new BusinessException(
                    "Tu domicilio aún no tiene zona de cobertura asignada, " +
                            "contacta al administrador"
            );
        }

        Ruta ruta = domicilio.getZonaCobertura().getRuta();

        // Obtener estado actual de la ruta
        EstadoRutaActual estado = estadoRutaRepository
                .findByRuta(ruta)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No hay información de ruta disponible"
                ));

        return calcularETA(domicilio, ruta, estado);
    }

    // ===== CALCULAR ETA =====
    private ETAResponse calcularETA(
            Domicilio domicilio,
            Ruta ruta,
            EstadoRutaActual estado) {

        int posicionActual = estado.getPositionIdActual();
        int totalPosiciones = posicionRutaRepository.countByRuta(ruta);

        // Ruta no iniciada
        if (estado.getStatus() == StatusRuta.PENDIENTE) {
            return ETAResponse.builder()
                    .routeId(ruta.getRouteId())
                    .nombreRuta(ruta.getNombre())
                    .mensaje("El camión aún no ha salido. " +
                            "Te notificaremos cuando inicie su recorrido.")
                    .status(estado.getStatus().name())
                    .posicionActual(posicionActual)
                    .totalPosiciones(totalPosiciones)
                    .build();
        }

        // Ruta finalizada
        if (estado.getStatus() == StatusRuta.FINALIZADO) {
            return ETAResponse.builder()
                    .routeId(ruta.getRouteId())
                    .nombreRuta(ruta.getNombre())
                    .mensaje("El servicio de recolección ha finalizado por hoy. " +
                            "No saques basura a la calle.")
                    .status(estado.getStatus().name())
                    .posicionActual(posicionActual)
                    .totalPosiciones(totalPosiciones)
                    .build();
        }

        // Ruta en curso — calcular distancia y minutos
        PosicionRuta posActual = posicionRutaRepository
                .findByRutaAndPositionId(ruta, posicionActual)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Posición actual no encontrada"
                ));

        // Distancia del camión al domicilio
        double distanciaKm = geocodificacionService.calcularDistanciaKm(
                posActual.getLat(), posActual.getLng(),
                domicilio.getLat(), domicilio.getLng()
        );

        // Velocidad promedio estimada 20 km/h en zona urbana
        double velocidadPromedio = 20.0;
        int minutosAproximados = (int) Math.ceil((distanciaKm / velocidadPromedio) * 60);

        // Ventana de llegada
        LocalTime ahora = LocalTime.now();
        LocalTime horaInicio = ahora.plusMinutes(minutosAproximados - 5);
        LocalTime horaFin = ahora.plusMinutes(minutosAproximados + 10);

        String mensaje = construirMensaje(minutosAproximados, distanciaKm);

        return ETAResponse.builder()
                .routeId(ruta.getRouteId())
                .nombreRuta(ruta.getNombre())
                .mensaje(mensaje)
                .horaEstimadaInicio(horaInicio.format(TIME_FORMAT))
                .horaEstimadaFin(horaFin.format(TIME_FORMAT))
                .minutosAproximados(minutosAproximados)
                .status(estado.getStatus().name())
                .posicionActual(posicionActual)
                .totalPosiciones(totalPosiciones)
                .build();
    }

    // ===== CONSTRUIR MENSAJE AMIGABLE =====
    private String construirMensaje(int minutos, double distanciaKm) {
        if (minutos <= 5) {
            return "¡El camión está muy cerca! " +
                    "Saca tus bolsas a la acera ahora.";
        } else if (minutos <= 15) {
            return "El camión llegará en aproximadamente " + minutos +
                    " minutos. Es momento de preparar tus bolsas.";
        } else if (minutos <= 30) {
            return "El camión llegará a tu zona entre " + minutos +
                    " y " + (minutos + 10) + " minutos.";
        } else {
            return "El camión aún está lejos de tu zona " +
                    String.format("(%.1f km)", distanciaKm) +
                    ". Te notificaremos cuando se aproxime.";
        }
    }

    // ===== HELPER =====
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