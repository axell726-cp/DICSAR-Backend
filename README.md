# DICSAR Backend - Sistema de Gestion de Inventario

## Descripcion

API REST del sistema de gestion de inventario para **DICSAR S.A.C.**, desarrollado en **Spring Boot** con arquitectura **MVC en capas**.

### Objetivos del Proyecto

- **Sprint 1:** Inventario y precios (productos, categorias, stock, vencimientos y notificaciones).
- **Sprint 2:** Proveedores y adquisiciones (modulo de compras, historial y movimientos de inventario).
- **Sprint 3:** Clientes y seguridad (modulo de clientes, usuarios, roles y control de acceso).

---

## Tecnologia

| Componente | Tecnologia |
|------------|------------|
| Framework | Spring Boot 3.5.6 |
| Lenguaje | Java 17 |
| Base de datos | MySQL 8.0 |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security + JWT |
| Build | Maven |

---

## Arquitectura

```
com.dicsar/
├── Application.java          # Punto de entrada
├── config/                   # Configuracion inicial
├── controller/               # Controladores REST
│   ├── AuthController.java   # Autenticacion
│   ├── ProductoController.java
│   ├── ProveedorController.java
│   ├── ClienteController.java
│   ├── MovimientoController.java
│   ├── ReporteController.java
│   ├── ReporteVentaController.java
│   ├── CategoriaController.java
│   ├── UnidadMedController.java
│   ├── NotificacionController.java
│   └── HistorialPrecioController.java
├── service/                  # Logica de negocio
├── repository/               # Repositorios JPA
├── entity/                   # Entidades
│   ├── Usuario.java
│   ├── Producto.java
│   ├── Proveedor.java
│   ├── Cliente.java
│   ├── Movimiento.java
│   ├── Categoria.java
│   ├── UnidadMed.java
│   ├── Notificacion.java
│   ├── HistorialPrecio.java
│   ├── ReporteVenta.java
│   └── ...
├── dto/                      # Objetos de transferencia
├── security/                 # JWT y seguridad
│   ├── SecurityConfig.java
│   ├── JwtUtil.java
│   ├── JwtRequestFilter.java
│   └── CustomUserDetailsService.java
├── exceptions/               # Manejo de errores
├── enums/                    # Enumeraciones
│   ├── TipoMovimiento.java
│   ├── TipoRegla.java
│   ├── TipoAlerta.java
│   ├── NivelAlerta.java
│   └── EstadoVencimiento.java
├── validator/               # Validadores
└── corsconfig/              # Configuracion CORS
```

---

## Entidades del Modelo de Datos

| Entidad | Descripcion |
|---------|-------------|
| **Usuario** | Usuarios del sistema con roles (Admin, Empleado) |
| **Producto** | Articulos con precios, stock, categoria, fecha de vencimiento |
| **Proveedor** | Proveedores con validacion de RUC |
| **Cliente** | Clientes para ventas |
| **Movimiento** | Entradas y salidas de inventario |
| **Categoria** | Categorias de productos |
| **UnidadMed** | Unidades de medida (kg, unid, L, etc.) |
| **Notificacion** | Alertas del sistema |
| **HistorialPrecio** | Registro historico de cambios de precios |
| **ReporteVenta** | Registro de ventas realizadas |

---

## Caracteristicas

- **Autenticacion JWT** con control de roles (Administrador, Empleado)
- **Gestion de productos** - CRUD completo con validaciones
- **Control de inventario** - Movimientos de entrada/salida
- **Gestion de proveedores** - Registro con validacion de RUC
- **Gestion de clientes** - Base de datos de clientes
- **Reportes** - Inventario, ventas, proveedores
- **Alertas** - Notificaciones de productos proximos a vencer
- **Historial de precios** - Seguimiento de cambios de precio
- **Validadores personalizados** - RUC, productos, reglas de precio

---

## Configuracion

### Base de Datos (application.properties)

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/dicsar
spring.datasource.username=root
spring.datasource.password=root
spring.jpa.hibernate.ddl-auto=update
```

### Puerto

La API corre en `http://localhost:8080`

---

## Guia de Uso

### Requisitos Previos

- Java 17
- Maven 3.8+
- MySQL 8.0

### Ejecutar el Proyecto

```bash
# Clonar el repositorio
git clone https://github.com/axell726-cp/DICSAR---Backend.git
cd DICSAR---Backend

# Compilar y ejecutar
mvn spring-boot:run
```

### Endpoints Principales

| Metodo | Endpoint | Descripcion |
|--------|----------|-------------|
| POST | `/api/auth/login` | Iniciar sesion |
| GET | `/api/productos` | Listar productos |
| POST | `/api/productos` | Crear producto |
| PUT | `/api/productos/{id}` | Actualizar producto |
| DELETE | `/api/productos/{id}` | Eliminar producto |
| GET | `/api/proveedores` | Listar proveedores |
| POST | `/api/proveedores` | Crear proveedor |
| GET | `/api/clientes` | Listar clientes |
| POST | `/api/movimientos` | Registrar movimiento |
| GET | `/api/reportes/inventario` | Reporte de inventario |
| GET | `/api/reportes/ventas` | Reporte de ventas |

---

## Flujo de Ramas (Git Flow)

- **`master`** - Rama estable oficial
- **`develop-sprintX`** - Rama de integracion por sprint
- **`feature/HU#-descripcion`** - Rama por Historia de Usuario

> No trabajar directamente en master. Hacer merge desde feature a develop-sprintX.

---

## Presentacion para GitHub

Este proyecto forma parte del sistema **DICSAR** - Gestion de Inventario para DICSAR S.A.C.

**Tecnologias utilizadas:**
- Spring Boot 3.5.6
- MySQL
- JWT Authentication
- Angular (Frontend separado)

**Usuario de prueba:**
- Usuario: `admin`
- Contrasena: `admin123`

---
