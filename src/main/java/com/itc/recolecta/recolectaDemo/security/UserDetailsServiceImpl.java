// security/UserDetailsServiceImpl.java
package com.itc.recolecta.recolectaDemo.security;

import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carga el usuario por su identificador principal: email o teléfono.
     * Se detecta por la presencia de '@' en el string.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) throws UsernameNotFoundException {
        Usuario usuario = resolveUsuario(identifier);

        // Usamos como username el identificador principal con el que se generó el JWT
        String principalIdentifier = usuario.getEmail() != null ? usuario.getEmail() : usuario.getTelefono();

        return new org.springframework.security.core.userdetails.User(
                principalIdentifier,
                usuario.getPasswordHash(),
                usuario.getActivo(),
                true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().name()))
        );
    }

    private Usuario resolveUsuario(String identifier) {
        if (identifier != null && identifier.contains("@")) {
            return usuarioRepository.findByEmail(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + identifier));
        } else {
            return usuarioRepository.findByTelefono(identifier)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + identifier));
        }
    }
}