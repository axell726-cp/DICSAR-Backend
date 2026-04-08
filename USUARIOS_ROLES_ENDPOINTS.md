# 🔐 DICSAR Backend - Gestión de Usuarios y Roles

## Endpoints Completos

### 👥 USUARIOS

#### 1. GET /api/usuarios
**Listar todos los usuarios**
```http
GET /api/usuarios
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "idUsuario": 1,
    "username": "admin",
    "nombreCompleto": "Administrador del Sistema",
    "rol": {
      "idRol": 1,
      "nombre": "ADMIN",
      "descripcion": "Acceso total"
    },
    "activo": true,
    "fechaCreacion": "2025-11-19T22:00:00"
  },
  {
    "idUsuario": 2,
    "username": "vendedor",
    "nombreCompleto": "Vendedor Demo",
    "rol": {
      "idRol": 2,
      "nombre": "VENDEDOR",
      "descripcion": "Acceso limitado"
    },
    "activo": true,
    "fechaCreacion": "2025-11-19T22:00:00"
  }
]
```

---

#### 2. GET /api/usuarios/{id}
**Obtener usuario específico**
```http
GET /api/usuarios/1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "idUsuario": 1,
  "username": "admin",
  "nombreCompleto": "Administrador del Sistema",
  "rol": {
    "idRol": 1,
    "nombre": "ADMIN",
    "descripcion": "Acceso total"
  },
  "activo": true
}
```

---

#### 3. POST /api/usuarios
**Crear nuevo usuario**
```http
POST /api/usuarios
Authorization: Bearer {token}
Content-Type: application/json

{
  "username": "gerente",
  "password": "gerente123",
  "nombreCompleto": "Gerente de Ventas",
  "idRol": 3,
  "activo": true
}
```

**Response (201 Created):**
```json
{
  "idUsuario": 3,
  "username": "gerente",
  "nombreCompleto": "Gerente de Ventas",
  "rol": {
    "idRol": 3,
    "nombre": "GERENTE",
    "descripcion": "Gerente de sucursal"
  },
  "activo": true,
  "fechaCreacion": "2025-11-19T23:30:00"
}
```

**Validaciones:**
- `username`: Obligatorio, único, 3-50 caracteres
- `password`: Obligatorio, encriptado automáticamente
- `nombreCompleto`: Obligatorio, máx 100 caracteres
- `idRol`: Obligatorio, debe existir
- `activo`: Opcional (default: true)

---

#### 4. PUT /api/usuarios/{id}
**Actualizar usuario**
```http
PUT /api/usuarios/3
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombreCompleto": "Gerente Regional de Ventas",
  "activo": true
}
```

**Response (200 OK):**
```json
{
  "idUsuario": 3,
  "username": "gerente",
  "nombreCompleto": "Gerente Regional de Ventas",
  "rol": {
    "idRol": 3,
    "nombre": "GERENTE"
  },
  "activo": true,
  "fechaModificacion": "2025-11-19T23:35:00"
}
```

**Campos actualizables:**
- `nombreCompleto`
- `activo` (activar/desactivar)
- `idRol` (cambiar rol)

**NO se puede actualizar:**
- `username` (fijo)
- `password` (usar endpoint específico)

---

#### 5. DELETE /api/usuarios/{id}
**Eliminar usuario (eliminación lógica)**
```http
DELETE /api/usuarios/3
Authorization: Bearer {token}
```

**Response (204 No Content):**
```
(vacío)
```

**Nota:** La eliminación es lógica (`activo = false`), no se borra de la BD.

---

### 🔑 ROLES

#### 1. GET /api/roles
**Listar todos los roles**
```http
GET /api/roles
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "idRol": 1,
    "nombre": "ADMIN",
    "descripcion": "Acceso total al sistema, reportes y exportaciones",
    "activo": true,
    "fechaCreacion": "2025-11-19T22:00:00"
  },
  {
    "idRol": 2,
    "nombre": "VENDEDOR",
    "descripcion": "Solo puede agregar productos, movimientos",
    "activo": true,
    "fechaCreacion": "2025-11-19T22:00:00"
  },
  {
    "idRol": 3,
    "nombre": "GERENTE",
    "descripcion": "Gerente de sucursal",
    "activo": true,
    "fechaCreacion": "2025-11-19T23:30:00"
  }
]
```

---

#### 2. GET /api/roles/activos
**Listar solo roles activos**
```http
GET /api/roles/activos
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
[
  {
    "idRol": 1,
    "nombre": "ADMIN",
    "activo": true
  },
  {
    "idRol": 2,
    "nombre": "VENDEDOR",
    "activo": true
  },
  {
    "idRol": 3,
    "nombre": "GERENTE",
    "activo": true
  }
]
```

---

#### 3. GET /api/roles/{id}
**Obtener rol específico**
```http
GET /api/roles/1
Authorization: Bearer {token}
```

**Response (200 OK):**
```json
{
  "idRol": 1,
  "nombre": "ADMIN",
  "descripcion": "Acceso total al sistema",
  "activo": true,
  "fechaCreacion": "2025-11-19T22:00:00"
}
```

---

#### 4. POST /api/roles
**Crear nuevo rol**
```http
POST /api/roles
Authorization: Bearer {token}
Content-Type: application/json

{
  "nombre": "SUPERVISOR",
  "descripcion": "Supervisor de tienda",
  "activo": true
}
```

**Response (201 Created):**
```json
{
  "idRol": 4,
  "nombre": "SUPERVISOR",
  "descripcion": "Supervisor de tienda",
  "activo": true,
  "fechaCreacion": "2025-11-19T23:45:00"
}
```

**Validaciones:**
- `nombre`: Obligatorio, único, no puede ser ADMIN o VENDEDOR (sistema)
- `descripcion`: Opcional
- `activo`: Opcional (default: true)

---

#### 5. PUT /api/roles/{id}
**Actualizar rol**
```http
PUT /api/roles/4
Authorization: Bearer {token}
Content-Type: application/json

{
  "descripcion": "Supervisor de tienda y almacén",
  "activo": true
}
```

**Response (200 OK):**
```json
{
  "idRol": 4,
  "nombre": "SUPERVISOR",
  "descripcion": "Supervisor de tienda y almacén",
  "activo": true,
  "fechaActualizacion": "2025-11-19T23:50:00"
}
```

**Campos actualizables:**
- `nombre`
- `descripcion`
- `activo`

**Restricción:** No se pueden modificar roles del sistema (ADMIN, VENDEDOR)

---

#### 6. DELETE /api/roles/{id}
**Eliminar rol**
```http
DELETE /api/roles/4
Authorization: Bearer {token}
```

**Response (204 No Content):**
```
(vacío)
```

**Restricción:** 
- No se pueden eliminar roles del sistema (ADMIN, VENDEDOR)
- No se puede eliminar un rol si hay usuarios asignados

---

## 🔐 Control de Acceso

### Solo ADMIN puede:
- ✅ Listar usuarios
- ✅ Crear usuarios
- ✅ Actualizar usuarios
- ✅ Eliminar usuarios
- ✅ Listar roles
- ✅ Crear roles
- ✅ Actualizar roles
- ✅ Eliminar roles
- ✅ Cambiar roles de usuarios

### VENDEDOR NO puede:
- ❌ Acceder a endpoints de usuarios
- ❌ Acceder a endpoints de roles
- ❌ Ver otros usuarios
- ❌ Crear/editar usuarios

---

## 📋 Flujo Típico: Crear Usuario con Nuevo Rol

### Paso 1: ADMIN crea nuevo rol
```bash
curl -X POST http://localhost:8080/api/roles \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "AUDITOR",
    "descripcion": "Auditor del sistema",
    "activo": true
  }'
```

**Respuesta:**
```json
{
  "idRol": 5,
  "nombre": "AUDITOR",
  "descripcion": "Auditor del sistema",
  "activo": true
}
```

### Paso 2: ADMIN obtiene ID del rol creado (5) y crea usuario
```bash
curl -X POST http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer {token}" \
  -H "Content-Type: application/json" \
  -d '{
    "username": "auditor",
    "password": "auditor123",
    "nombreCompleto": "Auditor Interno",
    "idRol": 5,
    "activo": true
  }'
```

**Respuesta:**
```json
{
  "idUsuario": 4,
  "username": "auditor",
  "nombreCompleto": "Auditor Interno",
  "rol": {
    "idRol": 5,
    "nombre": "AUDITOR"
  },
  "activo": true
}
```

### Paso 3: El nuevo usuario hace login
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "auditor",
    "password": "auditor123"
  }'
```

**Respuesta:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "auditor",
  "nombreCompleto": "Auditor Interno",
  "rol": "AUDITOR"
}
```

---

## ⚡ Códigos de Error

| Status | Descripción | Ejemplo |
|--------|-------------|---------|
| **200** | OK | Consulta exitosa |
| **201** | Created | Usuario/Rol creado |
| **204** | No Content | Eliminación exitosa |
| **400** | Bad Request | Datos inválidos |
| **401** | Unauthorized | Token inválido/expirado |
| **403** | Forbidden | No tiene permisos (no es ADMIN) |
| **404** | Not Found | Usuario/Rol no encontrado |
| **409** | Conflict | Usuario/Rol ya existe |
| **500** | Server Error | Error interno |

---

## 🧪 Casos de Prueba

### Test 1: Login como ADMIN
```
Username: admin
Password: admin123
Esperado: rol = "ADMIN"
```

### Test 2: Login como VENDEDOR
```
Username: vendedor
Password: vendedor123
Esperado: rol = "VENDEDOR"
```

### Test 3: ADMIN crea nuevo rol
```
POST /api/roles con nombre "GERENTE"
Esperado: 201 Created, idRol generado
```

### Test 4: ADMIN crea usuario con nuevo rol
```
POST /api/usuarios con idRol del GERENTE
Esperado: Usuario creado con ese rol
```

### Test 5: Nuevo usuario hace login
```
Login con credenciales del nuevo usuario
Esperado: rol correcto en respuesta
```

### Test 6: VENDEDOR intenta acceder a /api/roles
```
GET /api/roles con token de VENDEDOR
Esperado: 403 Forbidden
```

---

## 📝 Notas Importantes

1. **Roles del Sistema:**
   - ADMIN y VENDEDOR son creados automáticamente al iniciar
   - No se pueden eliminar
   - Se pueden modificar (nombre, descripción)

2. **Usuarios:**
   - Contraseña encriptada con BCrypt
   - No se envía contraseña en respuestas
   - Solo ADMIN puede verla

3. **Integridad:**
   - Si eliminas un usuario, sus datos se marcan como inactivos
   - Si cambias de rol, los permisos se aplican en el siguiente login
   - El token JWT contiene el rol del usuario

4. **Auditoría:**
   - Cada usuario tiene `fechaCreacion` y `fechaModificacion`
   - Las eliminaciones son lógicas (no se pierden datos)

---

**Status:** ✅ COMPLETO - Todos los endpoints implementados
**Última actualización:** 2025-11-19
