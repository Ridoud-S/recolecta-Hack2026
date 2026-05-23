// repository/PuntosOperadorRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.PuntosOperador;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PuntosOperadorRepository extends JpaRepository<PuntosOperador, Long> {
    Optional<PuntosOperador> findByCamionero(Usuario camionero);

    Optional<PuntosOperador> findByCamioneroAndMesAndAnio(
            Usuario camionero, Integer mes, Integer anio
    );

    @Query("""
        SELECT p FROM PuntosOperador p
        WHERE p.mes = :mes AND p.anio = :anio
        ORDER BY p.puntosTotales DESC
    """)
    List<PuntosOperador> findRankingMensual(
            @Param("mes") Integer mes,
            @Param("anio") Integer anio
    );
}