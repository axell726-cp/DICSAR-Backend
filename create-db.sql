-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS dicsar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE dicsar_db;

-- Las tablas se crearán automáticamente por Hibernate con spring.jpa.hibernate.ddl-auto=update
