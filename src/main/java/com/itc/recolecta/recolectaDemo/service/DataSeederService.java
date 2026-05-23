// service/DataSeederService.java
package com.itc.recolecta.recolectaDemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.enums.*;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class DataSeederService {

    private final UsuarioRepository usuarioRepository;
    private final RutaRepository rutaRepository;
    private final CamionRepository camionRepository;
    private final PosicionRutaRepository posicionRutaRepository;
    private final ZonaCoberturaRepository zonaCoberturaRepository;
    private final EstadoRutaRepository estadoRutaRepository;
    private final ScheduleRutaRepository scheduleRutaRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Bean
    @Order(1)
    public CommandLineRunner seedData() {
        return args -> {
            seedUsuarios();
            seedRutas();
            seedZonasCobertura();
            seedSchedules();
            log.info("🌱 Seed completo — Recolecta lista!");
        };
    }

    // ===== USUARIOS =====
    private void seedUsuarios() {
        if (usuarioRepository.existsByEmail("admin@recolecta.com")) return;

        // Admin
        usuarioRepository.save(Usuario.builder()
                .nombre("Administrador")
                .email("admin@recolecta.com")
                .passwordHash(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .build());

        // Ciudadano
        usuarioRepository.save(Usuario.builder()
                .nombre("Juan Pérez")
                .email("juan@recolecta.com")
                .telefono("4611234567")
                .passwordHash(passwordEncoder.encode("juan123"))
                .rol(Rol.CIUDADANO)
                .build());

        // Camioneros
        String[][] camioneros = {
                {"Carlos Ruiz",    "carlos@recolecta.com",  "4619876543"},
                {"Miguel Torres",  "miguel@recolecta.com",  "4612345678"},
                {"Pedro Sánchez",  "pedro@recolecta.com",   "4623456789"},
                {"Luis Ramírez",   "luis@recolecta.com",    "4634567890"},
                {"Jorge Mendoza",  "jorge@recolecta.com",   "4645678901"},
        };

        for (String[] c : camioneros) {
            usuarioRepository.save(Usuario.builder()
                    .nombre(c[0])
                    .email(c[1])
                    .telefono(c[2])
                    .passwordHash(passwordEncoder.encode("camionero123"))
                    .rol(Rol.CAMIONERO)
                    .build());
        }

        log.info("✅ Usuarios creados");
    }

    // ===== RUTAS Y POSICIONES desde JSON =====
    private void seedRutas() {
        if (rutaRepository.existsByRouteId("RUTA-01")) return;

        try {
            // JSON de rutas embebido directamente
            String rutasJson = """
            [
              {
                "routeId": "RUTA-01",
                "name": "Zona Centro - Las Arboledas",
                "truckId": 101,
                "status": "EN_RUTA",
                "positions": [
                  {"positionId":1,"lat":20.5111,"lng":-100.9037,"speed":0},
                  {"positionId":2,"lat":20.5185,"lng":-100.8450,"speed":45},
                  {"positionId":3,"lat":20.5215,"lng":-100.8142,"speed":22},
                  {"positionId":4,"lat":20.5212,"lng":-100.8175,"speed":15},
                  {"positionId":5,"lat":20.5210,"lng":-100.8210,"speed":0},
                  {"positionId":6,"lat":20.5235,"lng":-100.8212,"speed":18},
                  {"positionId":7,"lat":20.5260,"lng":-100.8215,"speed":20},
                  {"positionId":8,"lat":20.5111,"lng":-100.9037,"speed":40}
                ]
              },
              {
                "routeId": "RUTA-02",
                "name": "Sector Norte - Av. Tecnológico",
                "truckId": 102,
                "status": "EN_RUTA",
                "positions": [
                  {"positionId":1,"lat":20.5111,"lng":-100.9037,"speed":0},
                  {"positionId":2,"lat":20.5280,"lng":-100.8135,"speed":38},
                  {"positionId":3,"lat":20.5410,"lng":-100.8130,"speed":25},
                  {"positionId":4,"lat":20.5445,"lng":-100.8132,"speed":12},
                  {"positionId":5,"lat":20.5480,"lng":-100.8135,"speed":0},
                  {"positionId":6,"lat":20.5515,"lng":-100.8138,"speed":15},
                  {"positionId":7,"lat":20.5540,"lng":-100.8110,"speed":22},
                  {"positionId":8,"lat":20.5111,"lng":-100.9037,"speed":45}
                ]
              },
              {
                "routeId": "RUTA-03",
                "name": "Sector Poniente - San Juanico",
                "truckId": 103,
                "status": "EN_RUTA",
                "positions": [
                  {"positionId":1,"lat":20.5111,"lng":-100.9037,"speed":0},
                  {"positionId":2,"lat":20.5250,"lng":-100.8510,"speed":42},
                  {"positionId":3,"lat":20.5290,"lng":-100.8320,"speed":20},
                  {"positionId":4,"lat":20.5315,"lng":-100.8355,"speed":15},
                  {"positionId":5,"lat":20.5340,"lng":-100.8390,"speed":0},
                  {"positionId":6,"lat":20.5362,"lng":-100.8425,"speed":10},
                  {"positionId":7,"lat":20.5330,"lng":-100.8430,"speed":18},
                  {"positionId":8,"lat":20.5111,"lng":-100.9037,"speed":35}
                ]
              },
              {
                "routeId": "RUTA-04",
                "name": "Oriente - Los Olivos",
                "truckId": 104,
                "status": "EN_RUTA",
                "positions": [
                  {"positionId":1,"lat":20.5111,"lng":-100.9037,"speed":0},
                  {"positionId":2,"lat":20.5260,"lng":-100.8010,"speed":45},
                  {"positionId":3,"lat":20.5295,"lng":-100.7890,"speed":24},
                  {"positionId":4,"lat":20.5320,"lng":-100.7850,"speed":12},
                  {"positionId":5,"lat":20.5350,"lng":-100.7790,"speed":0},
                  {"positionId":6,"lat":20.5310,"lng":-100.7760,"speed":15},
                  {"positionId":7,"lat":20.5270,"lng":-100.7820,"speed":26},
                  {"positionId":8,"lat":20.5111,"lng":-100.9037,"speed":48}
                ]
              },
              {
                "routeId": "RUTA-05",
                "name": "Sector Sur - Rancho Seco",
                "truckId": 105,
                "status": "EN_RUTA",
                "positions": [
                  {"positionId":1,"lat":20.5111,"lng":-100.9037,"speed":0},
                  {"positionId":2,"lat":20.5050,"lng":-100.8620,"speed":35},
                  {"positionId":3,"lat":20.5020,"lng":-100.8350,"speed":22},
                  {"positionId":4,"lat":20.4995,"lng":-100.8210,"speed":14},
                  {"positionId":5,"lat":20.4970,"lng":-100.8150,"speed":0},
                  {"positionId":6,"lat":20.5010,"lng":-100.8120,"speed":16},
                  {"positionId":7,"lat":20.5060,"lng":-100.8160,"speed":25},
                  {"positionId":8,"lat":20.5111,"lng":-100.9037,"speed":40}
                ]
              }
            ]
            """;

            JsonNode rutas = objectMapper.readTree(rutasJson);

            for (JsonNode rutaNode : rutas) {
                String routeId = rutaNode.get("routeId").asText();
                int truckId = rutaNode.get("truckId").asInt();

                // Crear camión
                Camion camion = Camion.builder()
                        .truckId(truckId)
                        .capacidadLitros(200.0)
                        .rendimientoKmLitro(4.5)
                        .build();
                camionRepository.save(camion);

                // Crear ruta
                Ruta ruta = Ruta.builder()
                        .routeId(routeId)
                        .nombre(rutaNode.get("name").asText())
                        .status(StatusRuta.PENDIENTE)
                        .camion(camion)
                        .build();
                rutaRepository.save(ruta);

                // Crear posiciones
                JsonNode positions = rutaNode.get("positions");
                int orden = 1;
                for (JsonNode pos : positions) {
                    PosicionRuta posicion = PosicionRuta.builder()
                            .ruta(ruta)
                            .positionId(pos.get("positionId").asInt())
                            .lat(pos.get("lat").asDouble())
                            .lng(pos.get("lng").asDouble())
                            .speed(pos.get("speed").asDouble())
                            .timestamp(LocalDateTime.now())
                            .orden(orden++)
                            .build();
                    posicionRutaRepository.save(posicion);
                }

                // Crear estado inicial
                EstadoRutaActual estado = EstadoRutaActual.builder()
                        .ruta(ruta)
                        .positionIdActual(1)
                        .status(StatusRuta.PENDIENTE)
                        .ultimaActualizacion(LocalDateTime.now())
                        .build();
                estadoRutaRepository.save(estado);

                log.info("✅ Ruta {} cargada con {} posiciones",
                        routeId, positions.size());
            }

            // Asignar camioneros a rutas en orden
            List<Usuario> camionerosList = usuarioRepository.findAllByRol(Rol.CAMIONERO);
            List<Ruta> todasLasRutas = rutaRepository.findAll();
            for (int i = 0; i < Math.min(camionerosList.size(), todasLasRutas.size()); i++) {
                Ruta r = todasLasRutas.get(i);
                r.setCamionero(camionerosList.get(i));
                rutaRepository.save(r);
                log.info("🔗 Camionero {} asignado a ruta {}",
                        camionerosList.get(i).getNombre(), r.getRouteId());
            }

        } catch (Exception e) {
            log.error("Error cargando rutas: {}", e.getMessage());
        }
    }

    // ===== ZONAS DE COBERTURA desde JSON =====
    private void seedZonasCobertura() {
        if (zonaCoberturaRepository.findByNombreColoniaIgnoreCase(
                "Zona Centro").isPresent()) return;

        String[][] zonas = {
                {"Zona Centro",    "RUTA-01", "Matutino (06:30 - 07:15)"},
                {"Las Arboledas",  "RUTA-01", "Matutino (07:00 - 07:30)"},
                {"Trojes",         "RUTA-02", "Matutino (06:40 - 07:10)"},
                {"San Juanico",    "RUTA-03", "Matutino (06:45 - 07:15)"},
                {"Los Olivos",     "RUTA-04", "Matutino (07:00 - 07:40)"},
                {"Rancho Seco",    "RUTA-05", "Vespertino (14:15 - 15:00)"},
                {"Las Insurgentes","RUTA-02", "Matutino (06:35 - 07:10)"},
        };

        for (String[] z : zonas) {
            rutaRepository.findByRouteId(z[1]).ifPresent(ruta -> {
                ZonaCobertura zona = ZonaCobertura.builder()
                        .nombreColonia(z[0])
                        .ruta(ruta)
                        .horarioEstimado(z[2])
                        .build();
                zonaCoberturaRepository.save(zona);
            });
        }

        log.info("✅ Zonas de cobertura creadas");
    }

    // ===== SCHEDULES POR DÍA =====
    private void seedSchedules() {
        if (scheduleRutaRepository.findAll().isEmpty() == false) return;

        // RUTA-01: Lunes, Miércoles, Viernes
        crearSchedule("RUTA-01", DiaSemana.LUNES,     "06:00", "08:00");
        crearSchedule("RUTA-01", DiaSemana.MIERCOLES, "06:00", "08:00");
        crearSchedule("RUTA-01", DiaSemana.VIERNES,   "06:00", "08:00");

        // RUTA-02: Martes, Jueves
        crearSchedule("RUTA-02", DiaSemana.MARTES,  "06:00", "08:00");
        crearSchedule("RUTA-02", DiaSemana.JUEVES,  "06:00", "08:00");

        // RUTA-03: Lunes a Viernes
        crearSchedule("RUTA-03", DiaSemana.LUNES,     "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.MARTES,    "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.MIERCOLES, "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.JUEVES,    "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.VIERNES,   "06:30", "08:30");

        // RUTA-04: Lunes, Miércoles, Viernes
        crearSchedule("RUTA-04", DiaSemana.LUNES,     "07:00", "09:00");
        crearSchedule("RUTA-04", DiaSemana.MIERCOLES, "07:00", "09:00");
        crearSchedule("RUTA-04", DiaSemana.VIERNES,   "07:00", "09:00");

        // RUTA-05: Martes, Jueves (vespertino)
        crearSchedule("RUTA-05", DiaSemana.MARTES, "14:00", "16:00");
        crearSchedule("RUTA-05", DiaSemana.JUEVES, "14:00", "16:00");

        log.info("✅ Schedules creados");
    }

    private void crearSchedule(
            String routeId,
            DiaSemana dia,
            String inicio,
            String fin) {

        rutaRepository.findByRouteId(routeId).ifPresent(ruta -> {
            ScheduleRuta schedule = ScheduleRuta.builder()
                    .ruta(ruta)
                    .diaSemana(dia)
                    .horaInicio(LocalTime.parse(inicio))
                    .horaFin(LocalTime.parse(fin))
                    .build();
            scheduleRutaRepository.save(schedule);
        });
    }
}