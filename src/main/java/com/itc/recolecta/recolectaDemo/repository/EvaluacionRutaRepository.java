// repository/EvaluacionRutaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.EvaluacionRuta;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EvaluacionRutaRepository extends JpaRepository<EvaluacionRuta, Long> {
    List<EvaluacionRuta> findAllByCamionero(Usuario camionero);
    Optional<EvaluacionRuta> findByRuta(Ruta ruta);

    @Query("""
        SELECT AVG(e.calificacionCiudadano)
        FROM EvaluacionRuta e
        WHERE e.camionero = :camionero
        AND e.calificacionCiudadano IS NOT NULL
    """)
    Double findPromedioCalificacion(@Param("camionero") Usuario camionero);

    int countByCamioneroAndLlegoATiempoTrue(Usuario camionero);
}