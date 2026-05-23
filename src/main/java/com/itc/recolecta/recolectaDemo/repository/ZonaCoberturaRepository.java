// repository/ZonaCoberturaRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.Ruta;
import com.itc.recolecta.recolectaDemo.entity.ZonaCobertura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZonaCoberturaRepository extends JpaRepository<ZonaCobertura, Long> {
    List<ZonaCobertura> findAllByRuta(Ruta ruta);
    Optional<ZonaCobertura> findByNombreColoniaIgnoreCase(String nombreColonia);

    @Query("""
        SELECT z FROM ZonaCobertura z
        WHERE LOWER(z.nombreColonia) LIKE LOWER(CONCAT('%', :colonia, '%'))
    """)
    List<ZonaCobertura> findByColoniaSimilar(@Param("colonia") String colonia);
}