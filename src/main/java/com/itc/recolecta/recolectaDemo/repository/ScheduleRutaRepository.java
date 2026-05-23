// repository/ScheduleRutaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.entity.ScheduleRuta;
import com.itc.recolecta.recolectaDemo.enums.DiaSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleRutaRepository extends JpaRepository<ScheduleRuta, Long> {
    List<ScheduleRuta> findAllByRutaAndActivoTrue(Ruta ruta);
    List<ScheduleRuta> findAllByDiaSemanaAndActivoTrue(DiaSemana diaSemana);

    @Query("""
        SELECT s FROM ScheduleRuta s
        WHERE s.diaSemana = :dia
        AND s.activo = true
    """)
    List<ScheduleRuta> findRutasActivasHoy(@Param("dia") DiaSemana dia);

    Optional<ScheduleRuta> findByRutaAndDiaSemana(Ruta ruta, DiaSemana diaSemana);
}