// repository/RefreshTokenRepository.java
package com.itc.recolecta.recolectaDemo.repository;

import com.itc.recolecta.recolectaDemo.entity.RefreshToken;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);
    void deleteByUsuario(Usuario usuario);
    boolean existsByToken(String token);
}