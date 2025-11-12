-- Script de inicialización para crear usuario admin por defecto
-- Contraseña: admin123 (hasheada con BCrypt)
-- Ejecutar este script después de que Hibernate cree las tablas

INSERT INTO usuarios (username, password, nombre_completo, rol, activo, fecha_creacion, fecha_modificacion)
VALUES ('admin', '$2a$10$xGXTz7LPGv4X.fqW5BLZaOJb9q2gE1h/5gKYv7yZm7z4qVNJ.3Lv2', 'Administrador del Sistema', 'ADMIN', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE username = username;

-- Usuario vendedor de ejemplo
-- Contraseña: vendedor123
INSERT INTO usuarios (username, password, nombre_completo, rol, activo, fecha_creacion, fecha_modificacion)
VALUES ('vendedor', '$2a$10$N1p2L5qKzY4xH8wJ6vF3qOJ5yR8tX3sM9nA2bC7dE4fG1hI6jK0lM', 'Vendedor del Sistema', 'VENDEDOR', true, NOW(), NOW())
ON DUPLICATE KEY UPDATE username = username;
