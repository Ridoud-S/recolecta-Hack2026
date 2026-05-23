// repository/IncidenciaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.Incidencia;
import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.enums.TipoIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IncidenciaRepository extends JpaRepository<Incidencia, Long> {
    List<Incidencia> findAllByReportadoPor(Usuario usuario);
    List<Incidencia> findAllByRuta(Ruta ruta);
    List<Incidencia> findAllByTipo(TipoIncidencia tipo);
    List<Incidencia> findAllByRutaOrderByCreatedAtDesc(Ruta ruta);
    int countByReportadoPor(Usuario camionero);
}