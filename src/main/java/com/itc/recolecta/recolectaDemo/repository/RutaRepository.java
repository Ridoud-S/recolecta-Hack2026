// repository/RutaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.enums.StatusRuta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RutaRepository extends JpaRepository<Ruta, Long> {
    Optional<Ruta> findByRouteId(String routeId);
    List<Ruta> findAllByStatus(StatusRuta status);
    boolean existsByRouteId(String routeId);
}