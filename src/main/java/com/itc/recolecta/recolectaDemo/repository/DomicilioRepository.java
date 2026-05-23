// repository/DomicilioRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.Domicilio;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.locationtech.jts.geom.Point;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {

    @EntityGraph(attributePaths = {"zonaCobertura", "zonaCobertura.ruta"})
    List<Domicilio> findAllByUsuarioAndActivoTrue(Usuario usuario);

    @EntityGraph(attributePaths = {"zonaCobertura", "zonaCobertura.ruta"})
    Optional<Domicilio> findByIdAndUsuario(Long id, Usuario usuario);

    int countByUsuarioAndActivoTrue(Usuario usuario);

    @Query("SELECT d FROM Domicilio d WHERE d.zonaCobertura.id = :zonaCoberturaId AND d.activo = true")
    List<Domicilio> findAllByZonaCoberturaId(@Param("zonaCoberturaId") Long zonaCoberturaId);

    @Query("""
        SELECT d FROM Domicilio d
        WHERE d.zonaCobertura.ruta.id = :rutaId
        AND d.activo = true
    """)
    List<Domicilio> findAllByRutaId(@Param("rutaId") Long rutaId);

    @Query("SELECT d FROM Domicilio d WHERE dwithin(d.ubicacion, :punto, :distancia) = true AND d.activo = true")
    List<Domicilio> findNearbyDomicilios(@Param("punto") Point punto, @Param("distancia") double distancia);
}