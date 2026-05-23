// repository/PosicionRutaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.PosicionRuta;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PosicionRutaRepository extends JpaRepository<PosicionRuta, Long> {
    List<PosicionRuta> findAllByRutaOrderByOrdenAsc(Ruta ruta);
    Optional<PosicionRuta> findByRutaAndPositionId(Ruta ruta, Integer positionId);
    Optional<PosicionRuta> findByRutaAndOrden(Ruta ruta, Integer orden);
    int countByRuta(Ruta ruta);
}