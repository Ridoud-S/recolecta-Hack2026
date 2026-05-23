// service/NotificacionService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificacionService {

    private final DomicilioRepository domicilioRepository;
    private final NotificacionLogRepository notificacionLogRepository;
    private final UsuarioRepository usuarioRepository;

    // Eventos del JSON que nos dieron
    public static final String ROUTE_START = "ROUTE_START";
    public static final String TRUCK_PROXIMITY = "TRUCK_PROXIMITY";
    public static final String ROUTE_COMPLETED = "ROUTE_COMPLETED";

    // ===== NOTIFICAR INICIO DE RUTA =====
    public void notificarInicioRuta(Ruta ruta) {
        String titulo = "¡Ruta Iniciada!";
        String cuerpo = "El camión recolector ha salido rumbo a tu sector. " +
                "Asegúrate de tener listos tus residuos.";

        notificarUsuariosDeRuta(ruta, ROUTE_START, titulo, cuerpo);
    }

    // ===== NOTIFICAR CAMIÓN CERCANO =====
    public void notificarCamionCercano(Ruta ruta) {
        String titulo = "Camión Cercano";
        String cuerpo = "El camión está a menos de 15 minutos de tu domicilio. " +
                "Es momento de sacar tus bolsas a la acera.";

        notificarUsuariosDeRuta(ruta, TRUCK_PROXIMITY, titulo, cuerpo);
    }

    // ===== NOTIFICAR RUTA COMPLETADA =====
    public void notificarRutaCompletada(Ruta ruta) {
        String titulo = "Servicio Finalizado";
        String cuerpo = "El camión de tu sector ha concluido " +
                "su jornada de recolección diaria.";

        notificarUsuariosDeRuta(ruta, ROUTE_COMPLETED, titulo, cuerpo);
    }

    // ===== NOTIFICAR INCIDENCIA =====
    public void notificarIncidencia(Ruta ruta, String descripcion) {
        String titulo = "Aviso de tu ruta";
        String cuerpo = "El camión de tu sector reportó una incidencia: " +
                descripcion + ". Puede haber retrasos.";

        notificarUsuariosDeRuta(ruta, "INCIDENCIA", titulo, cuerpo);
    }

    // ===== CORE: Notificar a todos los usuarios de una ruta =====
    private void notificarUsuariosDeRuta(
            Ruta ruta,
            String tipoEvento,
            String titulo,
            String cuerpo) {

        // Obtener todos los domicilios de esta ruta
        List<Domicilio> domicilios = domicilioRepository
                .findAllByRutaId(ruta.getId());

        if (domicilios.isEmpty()) {
            log.warn("No hay domicilios registrados para ruta {}",
                    ruta.getRouteId());
            return;
        }

        for (Domicilio domicilio : domicilios) {
            Usuario usuario = domicilio.getUsuario();

            // Guardar log de notificación
            guardarLog(usuario, tipoEvento, titulo, cuerpo);

            // Enviar FCM si tiene token
            if (usuario.getFcmToken() != null &&
                    !usuario.getFcmToken().isEmpty()) {
                boolean isSilent = ruta.getEsNocturna() != null ? ruta.getEsNocturna() : false;
                enviarPushAFirebase(usuario.getFcmToken(), titulo, cuerpo, isSilent);
            } else {
                log.debug("Usuario {} sin FCM token, solo log guardado",
                        usuario.getEmail());
            }
        }

        log.info("Notificación {} enviada a {} usuarios de ruta {}",
                tipoEvento, domicilios.size(), ruta.getRouteId());
    }

    // ===== ENVIAR FCM =====
    @Async("taskExecutor")
    public void enviarPushAFirebase(String fcmToken, String titulo, String cuerpo, boolean isSilent) {
        // Por ahora solo log, Firebase se configura al final
        // Cuando tengamos FirebaseConfig esto se activa
        log.info("📱 FCM → titulo: '{}' | cuerpo: '{}' | silent: {}", titulo, cuerpo, isSilent);

        /* Código real con Firebase — activar cuando tengamos credentials:
        try {
            Message message = Message.builder()
                    .setToken(fcmToken)
                    .setNotification(Notification.builder()
                            .setTitle(titulo)
                            .setBody(cuerpo)
                            .build())
                    .build();
            String response = FirebaseMessaging.getInstance().send(message);
            log.info("FCM enviado: {}", response);
        } catch (FirebaseMessagingException e) {
            log.error("Error enviando FCM: {}", e.getMessage());
        }
        */
    }

    // ===== GUARDAR LOG =====
    private void guardarLog(
            Usuario usuario,
            String tipoEvento,
            String titulo,
            String cuerpo) {

        NotificacionLog log = NotificacionLog.builder()
                .usuario(usuario)
                .tipoEvento(tipoEvento)
                .titulo(titulo)
                .cuerpo(cuerpo)
                .build();

        notificacionLogRepository.save(log);
    }

    // ===== OBTENER HISTORIAL DE NOTIFICACIONES =====
    public List<NotificacionLog> obtenerHistorial(Usuario usuario) {
        return notificacionLogRepository
                .findTop10ByUsuarioOrderByEnviadoAtDesc(usuario);
    }
}