// repository/DomicilioRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.Domicilio;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DomicilioRepository extends JpaRepository<Domicilio, Long> {

    List<Domicilio> findAllByUsuarioAndActivoTrue(Usuario usuario);

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
}