// service/DomicilioService.java
package com.itc.recolecta.recolectaDemo.service;

import com.itc.recolecta.recolectaDemo.dto.request.DomicilioRequest;
import com.itc.recolecta.recolectaDemo.dto.response.DomicilioResponse;
import com.itc.recolecta.recolectaDemo.entity.Domicilio;
import com.itc.recolecta.recolectaDemo.entity.Usuario;
import com.itc.recolecta.recolectaDemo.entity.ZonaCobertura;
import com.itc.recolecta.recolectaDemo.exception.BusinessException;
import com.itc.recolecta.recolectaDemo.exception.ResourceNotFoundException;
import com.itc.recolecta.recolectaDemo.repository.DomicilioRepository;
import com.itc.recolecta.recolectaDemo.repository.UsuarioRepository;
import com.itc.recolecta.recolectaDemo.repository.ZonaCoberturaRepository;
import com.itc.recolecta.recolectaDemo.service.GeocodificacionService.Coordenadas;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DomicilioService {

    private final DomicilioRepository domicilioRepository;
    private final UsuarioRepository usuarioRepository;
    private final ZonaCoberturaRepository zonaCoberturaRepository;
    private final GeocodificacionService geocodificacionService;

    // Máximo de domicilios por usuario
    private static final int MAX_DOMICILIOS = 3;

    // ===== REGISTRAR DOMICILIO =====
    @Transactional
    public DomicilioResponse registrar(DomicilioRequest request) {
        Usuario usuario = getUsuarioAutenticado();

        // Validar límite de domicilios
        int total = domicilioRepository.countByUsuarioAndActivoTrue(usuario);
        if (total >= MAX_DOMICILIOS) {
            throw new BusinessException(
                    "Máximo " + MAX_DOMICILIOS + " domicilios permitidos por usuario"
            );
        }

        // Geocodificar ONE-TIME (nunca más se llama a Nominatim para este domicilio)
        Coordenadas coordenadas = geocodificacionService.geocodificar(
                request.getCalle(),
                request.getColonia(),
                "Celaya"
        );

        // Buscar zona de cobertura por colonia
        ZonaCobertura zona = buscarZonaCobertura(request.getColonia());

        // Crear Point para PostGIS
        GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        Point puntoUbicacion = geometryFactory.createPoint(new Coordinate(coordenadas.lng(), coordenadas.lat()));

        // Crear domicilio
        Domicilio domicilio = Domicilio.builder()
                .usuario(usuario)
                .alias(request.getAlias())
                .calle(request.getCalle())
                .colonia(request.getColonia())
                .codigoPostal(request.getCodigoPostal())
                .ubicacion(puntoUbicacion)
                .zonaCobertura(zona)
                .build();

        domicilioRepository.save(domicilio);
        log.info("Domicilio registrado para usuario {}: {}", usuario.getEmail(), request.getCalle());

        return toResponse(domicilio);
    }

    // ===== LISTAR MIS DOMICILIOS =====
    public List<DomicilioResponse> listarMios() {
        Usuario usuario = getUsuarioAutenticado();
        return domicilioRepository
                .findAllByUsuarioAndActivoTrue(usuario)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // ===== OBTENER DOMICILIO POR ID (RBAC) =====
    public DomicilioResponse obtenerPorId(Long id) {
        Usuario usuario = getUsuarioAutenticado();

        // RBAC: valida que el domicilio pertenece al usuario
        Domicilio domicilio = domicilioRepository
                .findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Domicilio no encontrado o no te pertenece"
                ));

        return toResponse(domicilio);
    }

    // ===== ELIMINAR DOMICILIO =====
    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = getUsuarioAutenticado();

        Domicilio domicilio = domicilioRepository
                .findByIdAndUsuario(id, usuario)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Domicilio no encontrado o no te pertenece"
                ));

        // Soft delete
        domicilio.setActivo(false);
        domicilioRepository.save(domicilio);
        log.info("Domicilio {} eliminado por usuario {}", id, usuario.getEmail());
    }

    // ===== HELPER: Buscar zona de cobertura =====
    private ZonaCobertura buscarZonaCobertura(String colonia) {
        List<ZonaCobertura> zonas = zonaCoberturaRepository
                .findByColoniaSimilar(colonia);

        if (zonas.isEmpty()) {
            log.warn("Colonia '{}' no encontrada en zonas de cobertura", colonia);
            // No lanzamos error, el domicilio se registra sin zona
            // El admin puede asignarlo después
            return null;
        }

        return zonas.get(0);
    }

    // ===== HELPER: Obtener usuario autenticado =====
    private Usuario getUsuarioAutenticado() {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario no encontrado"
                ));
    }

    // ===== HELPER: Mapear a Response =====
    private DomicilioResponse toResponse(Domicilio domicilio) {
        return DomicilioResponse.builder()
                .id(domicilio.getId())
                .alias(domicilio.getAlias())
                .calle(domicilio.getCalle())
                .colonia(domicilio.getColonia())
                .codigoPostal(domicilio.getCodigoPostal())
                .lat(domicilio.getUbicacion() != null ? domicilio.getUbicacion().getY() : null)
                .lng(domicilio.getUbicacion() != null ? domicilio.getUbicacion().getX() : null)
                .zonaCobertura(domicilio.getZonaCobertura() != null
                        ? domicilio.getZonaCobertura().getNombreColonia()
                        : "Sin zona asignada")
                .routeId(domicilio.getZonaCobertura() != null
                        ? domicilio.getZonaCobertura().getRuta().getRouteId()
                        : null)
                .horarioEstimado(domicilio.getZonaCobertura() != null
                        ? domicilio.getZonaCobertura().getHorarioEstimado()
                        : null)
                .build();
    }
}