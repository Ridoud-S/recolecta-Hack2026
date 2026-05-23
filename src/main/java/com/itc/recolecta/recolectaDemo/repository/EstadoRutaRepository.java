// repository/EstadoRutaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.EstadoRutaActual;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EstadoRutaRepository extends JpaRepository<EstadoRutaActual, Long> {
    Optional<EstadoRutaActual> findByRuta(Ruta ruta);
    Optional<EstadoRutaActual> findByRutaId(Long rutaId);
    List<EstadoRutaActual> findAllByStatus(StatusRuta status);
    List<EstadoRutaActual> findAllByStatusIn(List<StatusRuta> statuses);
}