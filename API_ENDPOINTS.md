# 📱 Recolecta API - Documentación de Endpoints

> **Fecha**: Mayo 2026  
> **Versión**: 1.0  
> **Documentación para**: Frontend Android (Retrofit/Moshi/Gson)

---

## 📋 Tabla de Contenidos

1. [Estructura Global](#estructura-global)
2. [Autenticación](#autenticación)
3. [Módulo Ciudadano](#módulo-ciudadano)
4. [Módulo Camionero](#módulo-camionero)
5. [Códigos de Estado HTTP](#códigos-de-estado-http)

---

## 🔐 Estructura Global

### ApiResponse Wrapper (Envuelto en TODAS las respuestas)

Toda respuesta del servidor viene envuelta en este formato genérico:

```json
{
  "success": true,
  "message": "Descripción del resultado",
  "data": {
    // Aquí va el objeto específico según el endpoint
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

#### Ejemplo de Respuesta Exitosa:
```json
{
  "success": true,
  "message": "Autenticación exitosa",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "d7a8f9e2-3b4c-5d6e-7f8a-9b0c1d2e3f4g",
    "nombre": "Juan Pérez",
    "email": "juan@recolecta.com",
    "rol": "CIUDADANO"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

#### Ejemplo de Respuesta con Error:
```json
{
  "success": false,
  "message": "Autenticación fallida",
  "data": null,
  "error": "Credenciales inválidas",
  "timestamp": "2026-05-23T14:30:00"
}
```

---

## 🔐 Autenticación

### 🔸 Registro - `POST /api/auth/register`

Crear una nueva cuenta de usuario.

**Headers:**
```
Content-Type: application/json
```

**Request:**
```json
{
  "nombre": "Juan Pérez",
  "email": "juan@recolecta.com",
  "password": "password123",
  "telefono": "4611234567",
  "rol": "CIUDADANO"
}
```

**Roles válidos:**
- `CIUDADANO` - Usuario civil que registra sus domicilios
- `CAMIONERO` - Operador de recolección
- `ADMIN` - Administrador del sistema

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Usuario registrado exitosamente",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "d7a8f9e2-3b4c-5d6e-7f8a-9b0c1d2e3f4g",
    "nombre": "Juan Pérez",
    "email": "juan@recolecta.com",
    "rol": "CIUDADANO"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🔸 Login - `POST /api/auth/login`

Iniciar sesión con email y contraseña.

**Headers:**
```
Content-Type: application/json
```

**Request:**
```json
{
  "email": "juan@recolecta.com",
  "password": "password123"
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Sesión iniciada",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "d7a8f9e2-3b4c-5d6e-7f8a-9b0c1d2e3f4g",
    "nombre": "Juan Pérez",
    "email": "juan@recolecta.com",
    "rol": "CIUDADANO"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Response (401 Unauthorized):**
```json
{
  "success": false,
  "message": "Autenticación fallida",
  "data": null,
  "error": "Email o contraseña inválidos",
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🔸 Refresh Token - `POST /api/auth/refresh`

Refrescar el access token usando el refresh token.

**Headers:**
```
Content-Type: application/json
Refresh-Token: d7a8f9e2-3b4c-5d6e-7f8a-9b0c1d2e3f4g
```

**Request:** (sin body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Token refrescado",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "refreshToken": "d7a8f9e2-3b4c-5d6e-7f8a-9b0c1d2e3f4g",
    "nombre": "Juan Pérez",
    "email": "juan@recolecta.com",
    "rol": "CIUDADANO"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🔸 Logout - `POST /api/auth/logout`

Cerrar sesión e invalidar el refresh token.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Refresh-Token: d7a8f9e2-3b4c-5d6e-7f8a-9b0c1d2e3f4g
```

**Request:** (sin body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Sesión cerrada exitosamente",
  "data": null,
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

## 👥 Módulo Ciudadano

> ⚠️ **Todos estos endpoints requieren:**
> ```
> Authorization: Bearer <accessToken>
> ```

### 🏠 Registrar Domicilio - `POST /api/ciudadano/domicilios`

Registrar un nuevo domicilio para recepción de basura.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "alias": "Casa",
  "calle": "Av. Tecnológico 123",
  "colonia": "Zona Centro",
  "codigoPostal": "38010"
}
```

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Domicilio registrado exitosamente",
  "data": {
    "id": 1,
    "alias": "Casa",
    "calle": "Av. Tecnológico 123",
    "colonia": "Zona Centro",
    "codigoPostal": "38010",
    "lat": 20.5280,
    "lng": -100.8135,
    "zonaCobertura": "Zona Centro",
    "routeId": "RUTA-01",
    "horarioEstimado": "Matutino (06:30 - 07:15)"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🏠 Listar Mis Domicilios - `GET /api/ciudadano/domicilios`

Obtener todos los domicilios registrados por el usuario.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Domicilios obtenidos",
  "data": [
    {
      "id": 1,
      "alias": "Casa",
      "calle": "Av. Tecnológico 123",
      "colonia": "Zona Centro",
      "codigoPostal": "38010",
      "lat": 20.5280,
      "lng": -100.8135,
      "zonaCobertura": "Zona Centro",
      "routeId": "RUTA-01",
      "horarioEstimado": "Matutino (06:30 - 07:15)"
    },
    {
      "id": 2,
      "alias": "Oficina",
      "calle": "Calle Principal 456",
      "colonia": "Las Arboledas",
      "codigoPostal": "38020",
      "lat": 20.5185,
      "lng": -100.8450,
      "zonaCobertura": "Las Arboledas",
      "routeId": "RUTA-01",
      "horarioEstimado": "Matutino (07:00 - 07:30)"
    }
  ],
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🏠 Obtener Domicilio por ID - `GET /api/ciudadano/domicilios/{id}`

Obtener detalles de un domicilio específico.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `id` (Long): ID del domicilio

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Domicilio obtenido",
  "data": {
    "id": 1,
    "alias": "Casa",
    "calle": "Av. Tecnológico 123",
    "colonia": "Zona Centro",
    "codigoPostal": "38010",
    "lat": 20.5280,
    "lng": -100.8135,
    "zonaCobertura": "Zona Centro",
    "routeId": "RUTA-01",
    "horarioEstimado": "Matutino (06:30 - 07:15)"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🏠 Eliminar Domicilio - `DELETE /api/ciudadano/domicilios/{id}`

Eliminar un domicilio registrado.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `id` (Long): ID del domicilio a eliminar

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Domicilio eliminado exitosamente",
  "data": null,
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### ⏱️ Obtener ETA - `GET /api/ciudadano/eta/{domicilioId}`

Obtener la hora estimada de llegada (ETA) del camión a tu domicilio. **¡Viene de Redis!**

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `domicilioId` (Long): ID del domicilio

**Response (200 OK):**
```json
{
  "success": true,
  "message": "ETA obtenido",
  "data": {
    "routeId": "RUTA-01",
    "nombreRuta": "Zona Centro - Las Arboledas",
    "mensaje": "El camión está en tu colonia. Llega en ~15 min.",
    "horaEstimadaInicio": "07:00",
    "horaEstimadaFin": "07:15",
    "minutosAproximados": 15,
    "status": "EN_CAMINO",
    "posicionActual": 4,
    "totalPosiciones": 10
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Status posibles:**
- `PENDIENTE` - La ruta aún no ha iniciado
- `EN_CAMINO` - El camión está en ruta hacia tu domicilio
- `PROXIMAMENTE` - El camión está muy cerca
- `FINALIZADA` - La ruta ya fue completada

---

### 📢 Obtener Notificaciones - `GET /api/ciudadano/notificaciones`

Obtener las últimas 10 notificaciones recibidas.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Notificaciones obtenidas",
  "data": [
    {
      "id": 1,
      "tipoEvento": "ETA_ACTUALIZADO",
      "titulo": "El camión está llegando",
      "cuerpo": "Tu ruta RUTA-01 llegará en 15 minutos",
      "enviadoAt": "2026-05-23T07:00:00"
    },
    {
      "id": 2,
      "tipoEvento": "RUTA_COMPLETADA",
      "titulo": "Recolección completada",
      "cuerpo": "La ruta RUTA-01 ha finalizado en tu zona",
      "enviadoAt": "2026-05-23T07:30:00"
    }
  ],
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Tipos de eventos:**
- `ETA_ACTUALIZADO` - El ETA cambió
- `RUTA_INICIADA` - La ruta empezó
- `RUTA_PROXIMAMENTE` - El camión está muy cerca
- `RUTA_COMPLETADA` - La ruta finalizó
- `INCIDENCIA_REPORTADA` - Hay una incidencia en tu zona

---

### ⭐ Calificar Servicio - `POST /api/ciudadano/calificacion/{rutaId}`

Calificar el servicio de recolección de una ruta.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**URL Parameters:**
- `rutaId` (String): ID de la ruta a calificar (ej: RUTA-01)

**Request:**
```json
{
  "calificacion": 5
}
```

**Calificaciones válidas:** 1-5 estrellas

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Calificación registrada exitosamente. ¡Gracias!",
  "data": null,
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

## 🚛 Módulo Camionero

> ⚠️ **Todos estos endpoints requieren:**
> ```
> Authorization: Bearer <accessToken>
> ```
> Y el usuario debe tener rol `CAMIONERO`

### 📍 Obtener Mi Ruta - `GET /api/camionero/mi-ruta`

Obtener la ruta asignada al camionero logueado.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ruta obtenida",
  "data": {
    "routeId": "RUTA-01",
    "nombre": "Zona Centro - Las Arboledas",
    "status": "PENDIENTE",
    "posicionActual": 1,
    "totalPosiciones": 8,
    "ultimaActualizacion": "2026-05-23T06:00:00"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Status posibles:**
- `PENDIENTE` - Ruta no iniciada
- `EN_RUTA` - Ruta en progreso
- `PAUSADA` - Ruta pausada por incidencia
- `COMPLETADA` - Ruta finalizada

---

### 🚀 Iniciar Ruta - `POST /api/camionero/ruta/{routeId}/iniciar`

Indicar que la ruta está iniciando.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Request:** (sin body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ruta iniciada exitosamente",
  "data": "RUTA-01",
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### ⏸️ Pausar Ruta - `POST /api/camionero/ruta/{routeId}/pausar`

Pausar la ruta por una incidencia o retraso.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Request:** (sin body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ruta pausada por incidencia",
  "data": "RUTA-01",
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### ⚠️ Reportar Incidencia - `POST /api/camionero/incidencias`

Reportar una incidencia (tráfico, daño mecánico, etc.) durante la ruta.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**Request:**
```json
{
  "tipo": "TRAFICO",
  "descripcion": "Calle bloqueada por obras en Av. Principal",
  "routeId": "RUTA-01"
}
```

**Tipos de incidencia:**
- `TRAFICO` - Problema de tráfico
- `DANO_MECANICO` - Falla en el camión
- `FALTA_DOMICILIO` - No se encontró el domicilio
- `DOMICILIO_RECHAZA` - Ciudadano rechaza el servicio
- `COMBUSTIBLE_BAJO` - Combustible bajo
- `OTRO` - Otro tipo de incidencia

**Response (201 Created):**
```json
{
  "success": true,
  "message": "Incidencia reportada exitosamente",
  "data": {
    "id": 1,
    "tipo": "TRAFICO",
    "descripcion": "Calle bloqueada por obras en Av. Principal",
    "reportadoPor": "Carlos Ruiz",
    "routeId": "RUTA-01",
    "createdAt": "2026-05-23T07:15:00"
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### ✅ Evaluar Ruta - `POST /api/camionero/evaluacion/{routeId}`

Evaluar la ruta al finalizar (gamificación).

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Request:**
```json
{
  "llegoATiempo": true,
  "tuvoIncidencia": false
}
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Evaluación registrada. ¡Gracias por tu reporte!",
  "data": null,
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Lógica de puntos:**
- ✅ Ruta a tiempo + Sin incidencias = **+50 puntos**
- ✅ Ruta a tiempo + Con incidencias = **+30 puntos**
- ❌ Ruta retrasada + Sin incidencias = **+15 puntos**
- ❌ Ruta retrasada + Con incidencias = **+0 puntos**

---

### 📊 Ver Mis Estadísticas - `GET /api/camionero/mis-stats`

Obtener tus estadísticas personales de gamificación (puntos, badges, rankings).

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Estadísticas obtenidas",
  "data": {
    "camioneroId": 3,
    "nombre": "Carlos Ruiz",
    "puntosTotales": 1250,
    "rutasCompletadas": 45,
    "rutasATiempo": 40,
    "incidenciasReportadas": 2,
    "promedioCalificacion": 4.8,
    "totalBadges": 3,
    "badges": [
      "Transparencia Total",
      "Puntualidad",
      "Búho Recolector"
    ],
    "mes": 5,
    "anio": 2026
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Campos:**
- `puntosTotales` - Puntos acumulados en el mes
- `rutasCompletadas` - Total de rutas finalizadas
- `rutasATiempo` - Rutas completadas sin retraso
- `incidenciasReportadas` - Incidencias reportadas (transparencia)
- `promedioCalificacion` - Promedio de calificaciones de ciudadanos (1-5)
- `totalBadges` - Cantidad de badges ganados este mes

---

### 🏆 Ver Mis Badges - `GET /api/camionero/mis-badges`

Obtener todos los badges ganados.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Badges obtenidos",
  "data": [
    {
      "id": 1,
      "tipoBadge": "PUNTUALIDAD",
      "descripcion": "Completaste 30 rutas a tiempo",
      "otorgadoAt": "2026-05-15T10:00:00"
    },
    {
      "id": 2,
      "tipoBadge": "TRANSPARENCIA",
      "descripcion": "Reportaste todas tus incidencias",
      "otorgadoAt": "2026-05-18T15:30:00"
    },
    {
      "id": 3,
      "tipoBadge": "BUHO_RECOLECTOR",
      "descripcion": "Completaste ruta nocturna sin problemas",
      "otorgadoAt": "2026-05-22T06:00:00"
    }
  ],
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

**Badges posibles:**
- `PUNTUALIDAD` - 30+ rutas a tiempo
- `TRANSPARENCIA` - 95%+ incidencias reportadas
- `BUHO_RECOLECTOR` - 5+ rutas nocturnas completadas
- `CERO_INCIDENCIAS` - Mes sin reportar incidencias
- `CIUDADANO_FELIZ` - Promedio de calificación 4.5+

---

## 👑 Módulo Administrador

> ⚠️ **Todos estos endpoints requieren:**
> ```
> Authorization: Bearer <accessToken>
> ```
> Y el usuario debe tener rol `ADMIN`

### 📊 Dashboard Administrativo - `GET /api/admin/dashboard`

Obtener el panel general de control administrativo.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Dashboard obtenido",
  "data": {
    "totalRutasActivas": 5,
    "totalRutasFinalizadas": 45,
    "totalIncidenciasHoy": 2,
    "combustibleTotalConsumido": 250.5,
    "kmTotalRecorridos": 1125.75,
    "rutasEnCurso": [
      {
        "routeId": "RUTA-01",
        "nombre": "Zona Centro - Las Arboledas",
        "status": "EN_RUTA",
        "posicionActual": 4,
        "totalPosiciones": 8,
        "ultimaActualizacion": "2026-05-23T07:15:00",
        "kmRecorridos": 30.0,
        "combustibleConsumido": 6.7
      },
      {
        "routeId": "RUTA-02",
        "nombre": "Sector Norte - Av. Tecnológico",
        "status": "EN_RUTA",
        "posicionActual": 5,
        "totalPosiciones": 8,
        "ultimaActualizacion": "2026-05-23T07:20:00",
        "kmRecorridos": 35.0,
        "combustibleConsumido": 7.8
      }
    ],
    "topOperadores": [
      {
        "posicion": 1,
        "camioneroId": 3,
        "nombre": "Carlos Ruiz",
        "puntosTotales": 1250,
        "rutasCompletadas": 45,
        "rutasATiempo": 40,
        "mes": 5,
        "anio": 2026
      },
      {
        "posicion": 2,
        "camioneroId": 4,
        "nombre": "Miguel Torres",
        "puntosTotales": 1100,
        "rutasCompletadas": 42,
        "rutasATiempo": 38,
        "mes": 5,
        "anio": 2026
      }
    ]
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🛣️ Listar Todas las Rutas - `GET /api/admin/rutas`

Obtener lista de todas las rutas con su status actual.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Rutas obtenidas",
  "data": [
    {
      "routeId": "RUTA-01",
      "nombre": "Zona Centro - Las Arboledas",
      "status": "EN_RUTA",
      "posicionActual": 4,
      "totalPosiciones": 8,
      "ultimaActualizacion": "2026-05-23T07:15:00",
      "kmRecorridos": 30.0,
      "combustibleConsumido": 6.7
    },
    {
      "routeId": "RUTA-02",
      "nombre": "Sector Norte - Av. Tecnológico",
      "status": "PENDIENTE",
      "posicionActual": 1,
      "totalPosiciones": 8,
      "ultimaActualizacion": "2026-05-23T06:00:00",
      "kmRecorridos": 0.0,
      "combustibleConsumido": 0.0
    },
    {
      "routeId": "RUTA-03",
      "nombre": "Sector Poniente - San Juanico",
      "status": "PAUSADA",
      "posicionActual": 3,
      "totalPosiciones": 8,
      "ultimaActualizacion": "2026-05-23T06:45:00",
      "kmRecorridos": 22.5,
      "combustibleConsumido": 5.0
    }
  ],
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🛣️ Obtener Status Detallado de Ruta - `GET /api/admin/rutas/{routeId}/status`

Obtener detalles completos del status de una ruta específica.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Status de ruta obtenido",
  "data": {
    "routeId": "RUTA-01",
    "nombre": "Zona Centro - Las Arboledas",
    "status": "EN_RUTA",
    "posicionActual": 4,
    "totalPosiciones": 8,
    "ultimaActualizacion": "2026-05-23T07:15:00",
    "kmRecorridos": 30.0,
    "combustibleConsumido": 6.7
  },
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### ⚠️ Obtener Incidencias de Ruta - `GET /api/admin/incidencias/ruta/{routeId}`

Obtener todas las incidencias reportadas en una ruta específica.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Incidencias de ruta obtenidas",
  "data": [
    {
      "id": 1,
      "tipo": "TRAFICO",
      "descripcion": "Calle bloqueada por obras en Av. Principal",
      "reportadoPor": "Carlos Ruiz",
      "routeId": "RUTA-01",
      "createdAt": "2026-05-23T07:15:00"
    },
    {
      "id": 2,
      "tipo": "DANO_MECANICO",
      "descripcion": "Falla en hidráulicos del camión",
      "reportadoPor": "Carlos Ruiz",
      "routeId": "RUTA-01",
      "createdAt": "2026-05-23T07:45:00"
    }
  ],
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🎮 Demo - Avanzar Ruta - `POST /api/admin/demo/ruta/{routeId}/avanzar`

**(SOLO PARA DEMOSTRACIÓN)** Avanzar la ruta a la siguiente posición.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Request:** (sin body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ruta RUTA-01 avanzada a posición 5 de 8",
  "data": null,
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

### 🎮 Demo - Reiniciar Ruta - `POST /api/admin/demo/ruta/{routeId}/reiniciar`

**(SOLO PARA DEMOSTRACIÓN)** Reiniciar la ruta a su posición inicial.

**Headers:**
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**URL Parameters:**
- `routeId` (String): ID de la ruta (ej: RUTA-01)

**Request:** (sin body)

**Response (200 OK):**
```json
{
  "success": true,
  "message": "Ruta RUTA-01 reiniciada al estado inicial",
  "data": null,
  "error": null,
  "timestamp": "2026-05-23T14:30:00"
}
```

---

## 📋 Códigos de Estado HTTP

| Código | Descripción | Escenario |
|--------|-------------|-----------|
| **200** | OK | Solicitud exitosa |
| **201** | Created | Recurso creado exitosamente |
| **204** | No Content | Solicitud exitosa sin datos de retorno |
| **400** | Bad Request | Validación fallida en los datos enviados |
| **401** | Unauthorized | Token no válido o no autenticado |
| **403** | Forbidden | Usuario no tiene permisos para esta acción |
| **404** | Not Found | Recurso no encontrado |
| **409** | Conflict | Conflicto (ej: email ya registrado) |
| **500** | Internal Server Error | Error en el servidor |

---

## 🔗 Variables de Prueba

### Credenciales por Defecto

```
CIUDADANO:
  Email: juan@recolecta.com
  Password: juan123
  Rol: CIUDADANO

CAMIONERO 1:
  Email: carlos@recolecta.com
  Password: camionero123
  Rol: CAMIONERO

CAMIONERO 2:
  Email: miguel@recolecta.com
  Password: camionero123
  Rol: CAMIONERO

ADMIN:
  Email: admin@recolecta.com
  Password: admin123
  Rol: ADMIN
```

### Rutas de Prueba

```
RUTA-01 - Zona Centro - Las Arboledas (8 posiciones)
RUTA-02 - Sector Norte - Av. Tecnológico (8 posiciones)
RUTA-03 - Sector Poniente - San Juanico (8 posiciones)
RUTA-04 - Oriente - Los Olivos (8 posiciones)
RUTA-05 - Sector Sur - Rancho Seco (8 posiciones)
```

### Coordenadas de Ejemplo

```
Centro (Depósito):
  Latitud: 20.5111
  Longitud: -100.9037

Zona Centro:
  Latitud: 20.5280
  Longitud: -100.8135

Las Arboledas:
  Latitud: 20.5185
  Longitud: -100.8450
```

---

## 📱 Implementación en Android (Retrofit/Moshi)

### Data Class Example - AuthResponse

```kotlin
data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?,
    val error: String?,
    val timestamp: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val nombre: String,
    val email: String,
    val rol: String
)

// Uso en Retrofit
interface RecolectaApiService {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<AuthResponse>
}
```

---

## 🔒 Notas de Seguridad

1. **Token JWT** se envía en header `Authorization: Bearer <token>`
2. **Refresh Token** se envía en header `Refresh-Token`
3. **TODOS los endpoints** requieren autenticación excepto `/api/auth/register` y `/api/auth/login`
4. Los tokens **expiran en 24 horas**
5. Usa HTTPS en producción
6. Nunca guardes tokens en `SharedPreferences` sin encriptar (usa `EncryptedSharedPreferences`)

---

**Última actualización:** Mayo 2026  
**Mantener actualizado conforme cambios en el backend**

