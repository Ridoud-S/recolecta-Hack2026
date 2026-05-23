// service/GeocodificacionService.java
package com.itc.recolecta.recolectaDemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itc.recolecta.recolectaDemo.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
@Slf4j
public class GeocodificacionService {

    @Value("${app.geocoding.nominatim-url}")
    private String nominatimUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    // Coordenadas resultado
    public record Coordenadas(Double lat, Double lng) {}

    // ===== GEOCODIFICAR DIRECCIÓN ONE-TIME =====
    public Coordenadas geocodificar(String calle, String colonia, String ciudad) {
        try {
            String direccion = calle + ", " + colonia + ", " + ciudad + ", México";

            String url = UriComponentsBuilder
                    .fromHttpUrl(nominatimUrl + "/search")
                    .queryParam("q", direccion)
                    .queryParam("format", "json")
                    .queryParam("limit", 1)
                    .queryParam("countrycodes", "mx")
                    .toUriString();

            // Header requerido por Nominatim
            org.springframework.http.HttpHeaders headers =
                    new org.springframework.http.HttpHeaders();
            headers.set("User-Agent", "Recolecta-App/1.0");

            org.springframework.http.HttpEntity<String> entity =
                    new org.springframework.http.HttpEntity<>(headers);

            org.springframework.http.ResponseEntity<String> response =
                    restTemplate.exchange(
                            url,
                            org.springframework.http.HttpMethod.GET,
                            entity,
                            String.class
                    );

            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.isEmpty()) {
                log.warn("No se encontraron coordenadas para: {}", direccion);
                throw new BusinessException(
                        "No se pudo encontrar la dirección, verifica los datos ingresados"
                );
            }

            JsonNode resultado = root.get(0);
            Double lat = resultado.get("lat").asDouble();
            Double lng = resultado.get("lon").asDouble();

            log.info("Coordenadas obtenidas para {}: lat={}, lng={}", direccion, lat, lng);
            return new Coordenadas(lat, lng);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error geocodificando dirección: {}", e.getMessage());
            throw new BusinessException("Error al obtener coordenadas de la dirección");
        }
    }

    // ===== CALCULAR DISTANCIA ENTRE DOS PUNTOS (Haversine) =====
    // Sin llamadas a APIs externas, cálculo interno
    public Double calcularDistanciaKm(
            Double lat1, Double lng1,
            Double lat2, Double lng2) {

        final int RADIO_TIERRA_KM = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLng / 2) * Math.sin(dLng / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }
}