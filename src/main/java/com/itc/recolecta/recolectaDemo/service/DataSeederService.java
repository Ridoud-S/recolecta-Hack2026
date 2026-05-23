// service/DataSeederService.java
package com.itc.recolecta.recolectaDemo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.itc.recolecta.recolectaDemo.entity.*;
import com.itc.recolecta.recolectaDemo.enums.*;
import com.itc.recolecta.recolectaDemo.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private final DomicilioRepository domicilioRepository;
    private final PuntosOperadorRepository puntosOperadorRepository;
    private final BadgeOperadorRepository badgeOperadorRepository;
    private final PasswordEncoder passwordEncoder;
    private final ObjectMapper objectMapper;

    @Bean
    @Order(1)
    public CommandLineRunner seedData() {
        return args -> {
            log.info("🌱 Iniciando seed de datos...");
            seedUsuarios();
            seedRutas();
            seedZonasCobertura();
            seedSchedules();
            seedDomicilios();
            seedGamificacion();
            log.info("✅ Seed completo — Recolecta lista!");
            mostrarCredenciales();
        };
    }

    // ===== USUARIOS =====
    private void seedUsuarios() {
        if (usuarioRepository.existsByEmail("admin@recolecta.com")) {
            log.info("⏭️  Usuarios ya existen, saltando seed");
            return;
        }

        // ADMIN
        usuarioRepository.save(Usuario.builder()
                .nombre("Administrador Sistema")
                .email("admin@recolecta.com")
                .telefono("4610000001")
                .passwordHash(passwordEncoder.encode("admin123"))
                .rol(Rol.ADMIN)
                .activo(true)
                .build());

        // CIUDADANOS (varios para hacer demos más vivas)
        String[][] ciudadanos = {
                {"Juan Pérez",     "juan@recolecta.com",     "4611111111"},
                {"María López",    "maria@recolecta.com",    "4612222222"},
                {"Roberto García", "roberto@recolecta.com",  "4613333333"},
                {"Ana Martínez",   "ana@recolecta.com",      "4614444444"},
        };

        for (String[] c : ciudadanos) {
            usuarioRepository.save(Usuario.builder()
                    .nombre(c[0])
                    .email(c[1])
                    .telefono(c[2])
                    .passwordHash(passwordEncoder.encode("ciudadano123"))
                    .rol(Rol.CIUDADANO)
                    .activo(true)
                    .build());
        }

        // CAMIONEROS (1 por cada ruta)
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
                    .activo(true)
                    .build());
        }

        log.info("✅ Usuarios creados: 1 admin, {} ciudadanos, {} camioneros",
                ciudadanos.length, camioneros.length);
    }

    // ===== RUTAS Y POSICIONES =====
    private void seedRutas() {
        if (rutaRepository.existsByRouteId("RUTA-01")) {
            log.info("⏭️  Rutas ya existen, saltando seed");
            return;
        }

        try {
            String rutasJson = """
            [
              {
                "routeId": "RUTA-01",
                "name": "Zona Centro - Las Arboledas",
                "truckId": 101,
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

                // Camión
                Camion camion = Camion.builder()
                        .truckId(truckId)
                        .capacidadLitros(200.0)
                        .rendimientoKmLitro(4.5)
                        .activo(true)
                        .build();
                camionRepository.save(camion);

                // Ruta
                Ruta ruta = Ruta.builder()
                        .routeId(routeId)
                        .nombre(rutaNode.get("name").asText())
                        .status(StatusRuta.PENDIENTE)
                        .camion(camion)
                        .build();
                rutaRepository.save(ruta);

                // Posiciones
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

                // Estado inicial
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

            // Asignar camioneros a rutas
            asignarCamioneros();

        } catch (Exception e) {
            log.error("❌ Error cargando rutas: {}", e.getMessage(), e);
        }
    }

    private void asignarCamioneros() {
        List<Usuario> camioneros = usuarioRepository.findAllByRol(Rol.CAMIONERO);
        List<Ruta> todasLasRutas = rutaRepository.findAll();

        for (int i = 0; i < Math.min(camioneros.size(), todasLasRutas.size()); i++) {
            Ruta r = todasLasRutas.get(i);
            try {
                // Si la entidad Ruta tiene setCamionero
                r.getClass().getMethod("setCamionero", Usuario.class)
                        .invoke(r, camioneros.get(i));
                rutaRepository.save(r);
                log.info("🔗 {} → {}", camioneros.get(i).getNombre(), r.getRouteId());
            } catch (NoSuchMethodException e) {
                log.warn("⚠️  La entidad Ruta no tiene setCamionero, omitiendo asignación");
                break;
            } catch (Exception e) {
                log.error("Error asignando camionero: {}", e.getMessage());
            }
        }
    }

    // ===== ZONAS DE COBERTURA =====
    private void seedZonasCobertura() {
        if (zonaCoberturaRepository.findByNombreColoniaIgnoreCase("Zona Centro").isPresent()) {
            log.info("⏭️  Zonas ya existen, saltando seed");
            return;
        }

        String[][] zonas = {
                {"Zona Centro",     "RUTA-01", "Matutino (06:30 - 07:15)"},
                {"Las Arboledas",   "RUTA-01", "Matutino (07:00 - 07:30)"},
                {"Trojes",          "RUTA-02", "Matutino (06:40 - 07:10)"},
                {"Las Insurgentes", "RUTA-02", "Matutino (06:35 - 07:10)"},
                {"San Juanico",     "RUTA-03", "Matutino (06:45 - 07:15)"},
                {"Los Olivos",      "RUTA-04", "Matutino (07:00 - 07:40)"},
                {"Rancho Seco",     "RUTA-05", "Vespertino (14:15 - 15:00)"},
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

        log.info("✅ {} zonas de cobertura creadas", zonas.length);
    }

    // ===== SCHEDULES =====
    private void seedSchedules() {
        if (!scheduleRutaRepository.findAll().isEmpty()) {
            log.info("⏭️  Schedules ya existen, saltando seed");
            return;
        }

        // RUTA-01: L, M, V matutino
        crearSchedule("RUTA-01", DiaSemana.LUNES,     "06:00", "08:00");
        crearSchedule("RUTA-01", DiaSemana.MIERCOLES, "06:00", "08:00");
        crearSchedule("RUTA-01", DiaSemana.VIERNES,   "06:00", "08:00");

        // RUTA-02: M, J matutino
        crearSchedule("RUTA-02", DiaSemana.MARTES, "06:00", "08:00");
        crearSchedule("RUTA-02", DiaSemana.JUEVES, "06:00", "08:00");

        // RUTA-03: Diario matutino
        crearSchedule("RUTA-03", DiaSemana.LUNES,     "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.MARTES,    "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.MIERCOLES, "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.JUEVES,    "06:30", "08:30");
        crearSchedule("RUTA-03", DiaSemana.VIERNES,   "06:30", "08:30");

        // RUTA-04: L, M, V matutino
        crearSchedule("RUTA-04", DiaSemana.LUNES,     "07:00", "09:00");
        crearSchedule("RUTA-04", DiaSemana.MIERCOLES, "07:00", "09:00");
        crearSchedule("RUTA-04", DiaSemana.VIERNES,   "07:00", "09:00");

        // RUTA-05: M, J vespertino
        crearSchedule("RUTA-05", DiaSemana.MARTES, "14:00", "16:00");
        crearSchedule("RUTA-05", DiaSemana.JUEVES, "14:00", "16:00");

        log.info("✅ Schedules creados");
    }

    private void crearSchedule(String routeId, DiaSemana dia, String inicio, String fin) {
        rutaRepository.findByRouteId(routeId).ifPresent(ruta -> {
            ScheduleRuta schedule = ScheduleRuta.builder()
                    .ruta(ruta)
                    .diaSemana(dia)
                    .horaInicio(LocalTime.parse(inicio))
                    .horaFin(LocalTime.parse(fin))
                    .activo(true)
                    .build();
            scheduleRutaRepository.save(schedule);
        });
    }

    // ===== DOMICILIOS DEMO (uno por ciudadano) =====
    private void seedDomicilios() {
        if (!domicilioRepository.findAll().isEmpty()) {
            log.info("⏭️  Domicilios ya existen, saltando seed");
            return;
        }

        try {
            // Juan → Zona Centro (RUTA-01)
            crearDomicilio("juan@recolecta.com", "Casa", "Av. Hidalgo 123",
                    "Zona Centro", "38000", 20.5185, -100.8450, "Zona Centro");

            // María → Las Arboledas (RUTA-01)
            crearDomicilio("maria@recolecta.com", "Casa", "Calle Roble 45",
                    "Las Arboledas", "38010", 20.5215, -100.8142, "Las Arboledas");

            // Roberto → Trojes (RUTA-02)
            crearDomicilio("roberto@recolecta.com", "Casa", "Privada del Sol 78",
                    "Trojes", "38020", 20.5410, -100.8130, "Trojes");

            // Ana → San Juanico (RUTA-03)
            crearDomicilio("ana@recolecta.com", "Casa", "Calle Real 200",
                    "San Juanico", "38030", 20.5290, -100.8320, "San Juanico");

            log.info("✅ Domicilios demo creados");
        } catch (Exception e) {
            log.warn("⚠️  No se pudieron crear domicilios: {}", e.getMessage());
        }
    }

    private final GeometryFactory geometryFactory =
            new GeometryFactory(new PrecisionModel(), 4326);

    private void crearDomicilio(String email, String alias, String calle,
                                String colonia, String cp, Double lat, Double lng,
                                String nombreZona) {
        usuarioRepository.findByEmail(email).ifPresent(usuario -> {
            ZonaCobertura zona = zonaCoberturaRepository
                    .findByNombreColoniaIgnoreCase(nombreZona)
                    .orElse(null);

            // PostGIS Point: longitud primero, latitud segundo
            Point ubicacion = geometryFactory.createPoint(new Coordinate(lng, lat));

            Domicilio dom = Domicilio.builder()
                    .usuario(usuario)
                    .alias(alias)
                    .calle(calle)
                    .colonia(colonia)
                    .codigoPostal(cp)
                    .ubicacion(ubicacion)
                    .zonaCobertura(zona)
                    .activo(true)
                    .build();
            domicilioRepository.save(dom);
        });

    }

    // ===== GAMIFICACIÓN — Para que el ranking se vea bonito en la demo =====
    private void seedGamificacion() {
        if (!puntosOperadorRepository.findAll().isEmpty()) {
            log.info("⏭️  Gamificación ya existe, saltando seed");
            return;
        }

        try {
            int mes = LocalDateTime.now().getMonthValue();
            int anio = LocalDateTime.now().getYear();

            // Carlos Ruiz — el #1 del ranking
            crearPuntos("carlos@recolecta.com", 1250, 45, 40, 2, mes, anio);
            otorgarBadge("carlos@recolecta.com", TipoBadge.RECOLECTOR_MES,
                    "Top 1 del mes con 1250 puntos");
            otorgarBadge("carlos@recolecta.com", TipoBadge.PUNTUALIDAD_PERFECTA,
                    "40 rutas a tiempo");

            // Miguel Torres — #2
            crearPuntos("miguel@recolecta.com", 1100, 42, 38, 1, mes, anio);
            otorgarBadge("miguel@recolecta.com", TipoBadge.PUNTUALIDAD_PERFECTA,
                    "38 rutas a tiempo");

            // Pedro Sánchez — #3
            crearPuntos("pedro@recolecta.com", 980, 38, 33, 3, mes, anio);
            otorgarBadge("pedro@recolecta.com", TipoBadge.REPORTE_HONESTO,
                    "Reporta sus incidencias correctamente");

            // Luis Ramírez
            crearPuntos("luis@recolecta.com", 820, 35, 28, 1, mes, anio);

            // Jorge Mendoza
            crearPuntos("jorge@recolecta.com", 680, 30, 22, 0, mes, anio);
            otorgarBadge("jorge@recolecta.com", TipoBadge.SIN_INCIDENCIAS,
                    "Mes completo sin incidencias");

            log.info("✅ Gamificación inicial cargada");
        } catch (Exception e) {
            log.warn("⚠️  No se pudo cargar gamificación: {}", e.getMessage());
        }
    }

    private void crearPuntos(String email, int puntos, int rutas, int aTiempo,
                             int incidencias, int mes, int anio) {
        usuarioRepository.findByEmail(email).ifPresent(camionero -> {
            PuntosOperador p = PuntosOperador.builder()
                    .camionero(camionero)
                    .puntosTotales(puntos)
                    .rutasCompletadas(rutas)
                    .rutasATiempo(aTiempo)
                    .incidenciasReportadas(incidencias)
                    .mes(mes)
                    .anio(anio)
                    .build();
            puntosOperadorRepository.save(p);
        });
    }

    private void otorgarBadge(String email, TipoBadge tipo, String descripcion) {
        usuarioRepository.findByEmail(email).ifPresent(camionero -> {
            BadgeOperador badge = BadgeOperador.builder()
                    .camionero(camionero)
                    .tipoBadge(tipo)
                    .descripcion(descripcion)
                    .otorgadoAt(LocalDateTime.now())
                    .build();
            badgeOperadorRepository.save(badge);
        });
    }

    // ===== INFO PARA LA DEMO =====
    private void mostrarCredenciales() {
        log.info("");
        log.info("╔════════════════════════════════════════════════╗");
        log.info("║  🔐 CREDENCIALES DEMO — RECOLECTA            ║");
        log.info("╠════════════════════════════════════════════════╣");
        log.info("║  ADMIN                                          ║");
        log.info("║    email: admin@recolecta.com                   ║");
        log.info("║    pass:  admin123                              ║");
        log.info("║                                                 ║");
        log.info("║  CIUDADANOS (todos con pass: ciudadano123)     ║");
        log.info("║    juan@recolecta.com    (Zona Centro)         ║");
        log.info("║    maria@recolecta.com   (Las Arboledas)       ║");
        log.info("║    roberto@recolecta.com (Trojes)              ║");
        log.info("║    ana@recolecta.com     (San Juanico)         ║");
        log.info("║                                                 ║");
        log.info("║  CAMIONEROS (todos con pass: camionero123)     ║");
        log.info("║    carlos@recolecta.com  (RUTA-01) — 1250 pts  ║");
        log.info("║    miguel@recolecta.com  (RUTA-02) — 1100 pts  ║");
        log.info("║    pedro@recolecta.com   (RUTA-03) — 980 pts   ║");
        log.info("║    luis@recolecta.com    (RUTA-04) — 820 pts   ║");
        log.info("║    jorge@recolecta.com   (RUTA-05) — 680 pts   ║");
        log.info("╚════════════════════════════════════════════════╝");
        log.info("");
    }
}