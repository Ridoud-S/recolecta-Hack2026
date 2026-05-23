// repository/NotificacionLogRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.NotificacionLog;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionLogRepository extends JpaRepository<NotificacionLog, Long> {
    List<NotificacionLog> findAllByUsuarioOrderByEnviadoAtDesc(Usuario usuario);
    List<NotificacionLog> findAllByTipoEvento(String tipoEvento);
    List<NotificacionLog> findTop10ByUsuarioOrderByEnviadoAtDesc(Usuario usuario);
}