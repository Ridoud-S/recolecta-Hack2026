// repository/BadgeOperadorRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.BadgeOperador;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.enums.TipoBadge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BadgeOperadorRepository extends JpaRepository<BadgeOperador, Long> {
    List<BadgeOperador> findAllByCamioneroOrderByOtorgadoAtDesc(Usuario camionero);
    boolean existsByCamioneroAndTipoBadge(Usuario camionero, TipoBadge tipoBadge);
    int countByCamionero(Usuario camionero);
}