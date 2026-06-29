-- Crear base de datos si no existe
CREATE DATABASE IF NOT EXISTS dicsar_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Usar la base de datos
USE dicsar_db;

-- Las tablas se crearán automáticamente por Hibernate con spring.jpa.hibernate.ddl-auto=update

-- Índice compuesto para consultas por cliente y fecha de venta (si la tabla VENTAS existe)
-- Se aplica en caso de que la tabla haya sido creada por la aplicación
-- Esto es seguro: MySQL ignorará ALTER TABLE si ya existe el índice con otro nombre (pero no si existe con mismo nombre)
DELIMITER $$
CREATE PROCEDURE ensure_idx_ventas()
BEGIN
	IF EXISTS(SELECT 1 FROM information_schema.tables WHERE table_schema = DATABASE() AND table_name = 'VENTAS') THEN
		SET @s = CONCAT('ALTER TABLE VENTAS ADD INDEX IF NOT EXISTS idx_ventas_cliente_fecha (cliente_id, fecha_venta)');
		-- MySQL < 8 no soporta ADD INDEX IF NOT EXISTS; run safely via handler
		BEGIN
			DECLARE CONTINUE HANDLER FOR SQLEXCEPTION BEGIN END;
			PREPARE stmt FROM @s;
			EXECUTE stmt;
			DEALLOCATE PREPARE stmt;
		END;
	END IF;
END$$
DELIMITER ;
CALL ensure_idx_ventas();
DROP PROCEDURE IF EXISTS ensure_idx_ventas;

-- Tabla para llevar la numeración secuencial de comprobantes (comprobante_sequence)
CREATE TABLE IF NOT EXISTS comprobante_sequence (
	id VARCHAR(50) PRIMARY KEY,
	`last_value` BIGINT NOT NULL
);
INSERT IGNORE INTO comprobante_sequence (id, `last_value`) VALUES ('comprobante', 0);

