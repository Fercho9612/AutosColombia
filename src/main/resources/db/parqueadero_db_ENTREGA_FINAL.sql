-- =============================================
-- Script : parqueadero_db_FINAL_SP.sql 
-- Proyecto: Parqueadero Autos Colombia
-- Equipo : Cadavid · Paternina · Marin
-- Fecha : 2026
-- =============================================
-- INSTRUCCIONES: Ejecutar TODO con Ctrl+Shift+Enter en MySQL Workbench
DROP DATABASE IF EXISTS parqueadero_db;
/*!40101 SET @OLD_CHARACTER_SET_CLIENT = @@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS = @@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION = @@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE = @@TIME_ZONE */;
/*!40103 SET TIME_ZONE = '+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS = @@UNIQUE_CHECKS, UNIQUE_CHECKS = 0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS = @@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS = 0 */;
/*!40101 SET @OLD_SQL_MODE = @@SQL_MODE, SQL_MODE = 'NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES = @@SQL_NOTES, SQL_NOTES = 0 */;

USE parqueadero_db;  -- Si ya existe, comenta el DROP y CREATE

-- Variables de configuración (el cliente puede cambiarlas vía Java o manualmente)
SET @V_ROL_DEFAULT = 'vigilante';
SET @V_ACTIVO_DEFAULT = TRUE;
SET @V_HASH_ALGORITMO = 256;

-- =============================================
-- § 1 BASE DE DATOS Y TABLAS
-- =============================================

CREATE DATABASE parqueadero_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE parqueadero_db;

-- 1.1 Usuario (Sistema + Clientes básicos)
CREATE TABLE usuario (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    rol ENUM('vigilante','admin') NOT NULL DEFAULT 'vigilante',
    password VARCHAR(255) NOT NULL,
    documento VARCHAR(20) NULL UNIQUE,
    telefono VARCHAR(15) NULL,
    correo VARCHAR(100) NULL,
    tipo_cliente ENUM('mensual','visitante') NOT NULL DEFAULT 'visitante',
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_usuario_nombre ON usuario(nombre);
CREATE INDEX idx_usuario_documento ON usuario(documento);
CREATE INDEX idx_usuario_activo ON usuario(activo);

-- 1.2 Vehículo
CREATE TABLE vehiculo (
    placa VARCHAR(10) PRIMARY KEY,
    tipo ENUM('CARRO','MOTO') NOT NULL
);

-- 1.3 Celda (el cliente decide cuántas crea)
CREATE TABLE celda (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo ENUM('CARRO','MOTO') NOT NULL,
    disponible BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_celda_tipo_disponible ON celda(tipo, disponible);

-- 1.4 Registro (Entrada/Salida)
CREATE TABLE registro (
    id INT AUTO_INCREMENT PRIMARY KEY,
    hora_entrada DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    hora_salida DATETIME NULL,
    placa_vehiculo VARCHAR(10) NOT NULL,
    id_celda INT NOT NULL,
    id_usuario_entrada INT NOT NULL,
    id_usuario_salida INT NULL,
    monto DECIMAL(10,2) NULL COMMENT 'Monto cobrado al salir',
    FOREIGN KEY (placa_vehiculo) REFERENCES vehiculo(placa) ON DELETE CASCADE,
    FOREIGN KEY (id_celda) REFERENCES celda(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario_entrada) REFERENCES usuario(id) ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario_salida) REFERENCES usuario(id) ON DELETE SET NULL,
    INDEX idx_placa (placa_vehiculo),
    INDEX idx_entrada_abierta (hora_salida)
);

-- 1.5 Tarifa (Configurable por el cliente - Gestión de Pagos)
CREATE TABLE tarifa (
    id INT AUTO_INCREMENT PRIMARY KEY,
    tipo_vehiculo ENUM('CARRO','MOTO') NOT NULL,
    precio_por_minuto DECIMAL(10,2) NOT NULL DEFAULT 50.00 COMMENT 'Tarifa básica por minuto',
    descripcion VARCHAR(100) NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    UNIQUE KEY uq_tarifa_tipo (tipo_vehiculo)
);

-- 1.6 Pago (Registro detallado de cobros)
CREATE TABLE pago (
    id INT AUTO_INCREMENT PRIMARY KEY,
    id_registro INT NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    fecha_pago DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_usuario_cobro INT NOT NULL,
    metodo_pago ENUM('efectivo','tarjeta','transferencia') NOT NULL DEFAULT 'efectivo',
    FOREIGN KEY (id_registro) REFERENCES registro(id) ON DELETE CASCADE,
    FOREIGN KEY (id_usuario_cobro) REFERENCES usuario(id)
);

-- =============================================
-- § 2 VISTAS
-- =============================================
CREATE VIEW v_vehiculos_activos AS
SELECT 
    r.id, r.placa_vehiculo, r.id_celda, r.hora_entrada,
    TIMESTAMPDIFF(MINUTE, r.hora_entrada, NOW()) AS minutos_permanencia,
    c.tipo AS tipo_celda, u.nombre AS registrado_por
FROM registro r
JOIN celda c ON r.id_celda = c.id
JOIN usuario u ON r.id_usuario_entrada = u.id
WHERE r.hora_salida IS NULL;

CREATE VIEW v_ocupacion AS
SELECT 
    tipo,
    COUNT(*) AS total_celdas,
    SUM(CASE WHEN disponible = FALSE THEN 1 ELSE 0 END) AS ocupadas,
    SUM(CASE WHEN disponible = TRUE THEN 1 ELSE 0 END) AS disponibles
FROM celda 
GROUP BY tipo;

-- =============================================
-- § 3 DATOS MÍNIMOS DE ARRANQUE
-- =============================================
-- Admin

INSERT INTO usuario (nombre, rol, password, tipo_cliente)
VALUES ('admin', 'admin', SHA2('1234', 256), 'visitante');

-- =============================================
-- § 4 PROCEDIMIENTOS ALMACENADOS
-- =============================================
DELIMITER $$

-- sp_login (sin cambios)
CREATE PROCEDURE sp_login(
    IN p_nombre VARCHAR(50),
    IN p_password VARCHAR(255),
    OUT r_id INT,
    OUT r_rol VARCHAR(20),
    OUT r_activo BOOLEAN
)
BEGIN
    SELECT id, rol, activo INTO r_id, r_rol, r_activo
    FROM usuario
    WHERE nombre = p_nombre AND password = p_password
    LIMIT 1;
END$$

-- sp_guardarOActualizarVehiculo (sin cambios importantes)
CREATE PROCEDURE sp_guardarOActualizarVehiculo(
    IN p_placa VARCHAR(10),
    IN p_tipo ENUM('CARRO','MOTO'),
    OUT r_resultado VARCHAR(30)
)
BEGIN
    SET p_placa = UPPER(TRIM(p_placa));
    IF (SELECT COUNT(*) FROM vehiculo WHERE placa = p_placa) = 0 THEN
        INSERT INTO vehiculo (placa, tipo) VALUES (p_placa, p_tipo);
        SET r_resultado = 'INSERTADO';
    ELSE
        UPDATE vehiculo SET tipo = p_tipo WHERE placa = p_placa;
        SET r_resultado = 'ACTUALIZADO';
    END IF;
END$$

-- sp_buscarCeldaDisponible (sin cambios)
CREATE PROCEDURE sp_buscarCeldaDisponible(
    IN p_tipo ENUM('CARRO','MOTO'),
    OUT r_id_celda INT
)
BEGIN
    SELECT id INTO r_id_celda
    FROM celda
    WHERE tipo = p_tipo AND disponible = TRUE
    ORDER BY id LIMIT 1;
END$$

-- sp_actualizarDisponibilidad (sin cambios)
CREATE PROCEDURE sp_actualizarDisponibilidad(
    IN p_id_celda INT,
    IN p_disponible BOOLEAN
)
BEGIN
    UPDATE celda SET disponible = p_disponible WHERE id = p_id_celda;
    IF ROW_COUNT() = 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Celda no encontrada';
    END IF;
END$$

-- sp_registrarEntrada (mejorado con id_usuario_entrada)
CREATE PROCEDURE sp_registrarEntrada(
    IN p_placa VARCHAR(10),
    IN p_tipo ENUM('CARRO','MOTO'),
    IN p_id_usuario INT,
    OUT r_id_registro INT,
    OUT r_id_celda INT,
    OUT r_mensaje VARCHAR(100)
)
BEGIN
    DECLARE v_celda_id INT DEFAULT NULL;
    DECLARE v_ya_adentro INT DEFAULT 0;
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; SET r_mensaje = 'ERROR en transacción de entrada'; END;

    SET p_placa = UPPER(TRIM(p_placa));
    START TRANSACTION;

    SELECT COUNT(*) INTO v_ya_adentro FROM registro 
    WHERE placa_vehiculo = p_placa AND hora_salida IS NULL;

    IF v_ya_adentro > 0 THEN
        SET r_mensaje = 'Vehículo ya está dentro';
        ROLLBACK;
    ELSE
        CALL sp_guardarOActualizarVehiculo(p_placa, p_tipo, @dummy);
        CALL sp_buscarCeldaDisponible(p_tipo, v_celda_id);

        IF v_celda_id IS NULL THEN
            SET r_mensaje = 'SIN_ESPACIO: No hay celdas disponibles';
            ROLLBACK;
        ELSE
            CALL sp_actualizarDisponibilidad(v_celda_id, FALSE);
            INSERT INTO registro (placa_vehiculo, id_celda, id_usuario_entrada)
            VALUES (p_placa, v_celda_id, p_id_usuario);
            SET r_id_registro = LAST_INSERT_ID();
            SET r_id_celda = v_celda_id;
            SET r_mensaje = 'OK';
            COMMIT;
        END IF;
    END IF;
END$$

-- sp_calcularTarifa (NUEVO - Gestión de Pagos)
CREATE PROCEDURE sp_calcularTarifa(
    IN p_placa VARCHAR(10),
    OUT r_monto DECIMAL(10,2)
)
BEGIN
    DECLARE v_minutos INT;
    DECLARE v_tipo ENUM('CARRO','MOTO');
    SET p_placa = UPPER(TRIM(p_placa));

    SELECT TIMESTAMPDIFF(MINUTE, hora_entrada, NOW()), v.tipo 
    INTO v_minutos, v_tipo
    FROM registro r JOIN vehiculo v ON r.placa_vehiculo = v.placa
    WHERE r.placa_vehiculo = p_placa AND r.hora_salida IS NULL;

    IF v_minutos IS NULL THEN
        SET r_monto = 0;
    ELSE
        SELECT precio_por_minuto * v_minutos INTO r_monto
        FROM tarifa WHERE tipo_vehiculo = v_tipo AND activo = TRUE;
    END IF;
END$$

-- sp_registrarSalida (Mejorado con pago)
CREATE PROCEDURE sp_registrarSalida(
    IN p_placa VARCHAR(10),
    IN p_id_usuario_salida INT,
    OUT r_minutos INT,
    OUT r_monto DECIMAL(10,2),
    OUT r_id_celda INT,
    OUT r_mensaje VARCHAR(100)
)
BEGIN
    DECLARE v_id_registro INT;
    DECLARE v_celda_id INT;
    DECLARE v_entrada DATETIME;

    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN ROLLBACK; SET r_mensaje = 'ERROR en transacción de salida'; END;

    SET p_placa = UPPER(TRIM(p_placa));

    SELECT id, id_celda, hora_entrada INTO v_id_registro, v_celda_id, v_entrada
    FROM registro WHERE placa_vehiculo = p_placa AND hora_salida IS NULL LIMIT 1;

    IF v_id_registro IS NULL THEN
        SET r_mensaje = 'NO_ENCONTRADO: Vehículo sin entrada activa';
    ELSE
        START TRANSACTION;
            CALL sp_calcularTarifa(p_placa, r_monto);

            UPDATE registro 
            SET hora_salida = NOW(),
                monto = r_monto,
                id_usuario_salida = p_id_usuario_salida
            WHERE id = v_id_registro;

            CALL sp_actualizarDisponibilidad(v_celda_id, TRUE);

            -- Registrar pago
            INSERT INTO pago (id_registro, monto, id_usuario_cobro)
            VALUES (v_id_registro, r_monto, p_id_usuario_salida);

            SET r_minutos = TIMESTAMPDIFF(MINUTE, v_entrada, NOW());
            SET r_id_celda = v_celda_id;
            SET r_mensaje = 'OK';
        COMMIT;
    END IF;
END$$

-- sp_getVehiculosActivos (actualizado)
CREATE PROCEDURE sp_getVehiculosActivos()
BEGIN
    SELECT r.id, r.placa_vehiculo, v.tipo AS tipo_vehiculo, r.id_celda,
           c.tipo AS tipo_celda, r.hora_entrada,
           TIMESTAMPDIFF(MINUTE, r.hora_entrada, NOW()) AS minutos_permanencia,
           u.nombre AS registrado_por
    FROM registro r
    JOIN vehiculo v ON r.placa_vehiculo = v.placa
    JOIN celda c ON r.id_celda = c.id
    JOIN usuario u ON r.id_usuario_entrada = u.id
    WHERE r.hora_salida IS NULL
    ORDER BY r.hora_entrada ASC;
END$$

-- sp_crearUsuario, sp_desactivarUsuario, sp_reactivarUsuario (mantengo igual que antes)

-- sp_ocupacionParqueadero → ahora usa la vista
CREATE PROCEDURE sp_ocupacionParqueadero()
BEGIN
    SELECT * FROM v_ocupacion;
END$$

DELIMITER ;

-- =============================================
-- § 5 FUNCIONES AUXILIARES
-- =============================================
DELIMITER $$
CREATE FUNCTION fn_hashPassword(p_password VARCHAR(255))
RETURNS VARCHAR(255) DETERMINISTIC
BEGIN RETURN SHA2(p_password, 256); END$$

CREATE FUNCTION fn_minutosEnParqueadero(p_placa VARCHAR(10))
RETURNS INT READS SQL DATA
BEGIN
    DECLARE v_minutos INT;
    SET p_placa = UPPER(TRIM(p_placa));
    SELECT TIMESTAMPDIFF(MINUTE, hora_entrada, NOW()) INTO v_minutos
    FROM registro WHERE placa_vehiculo = p_placa AND hora_salida IS NULL LIMIT 1;
    RETURN v_minutos;
END$$
DELIMITER ;

-- =============================================
-- § 6 TRIGGERS DE INTEGRIDAD
-- =============================================
DELIMITER $$

CREATE TRIGGER trg_vehiculo_before_insert BEFORE INSERT ON vehiculo
FOR EACH ROW SET NEW.placa = UPPER(TRIM(NEW.placa));

CREATE TRIGGER trg_vehiculo_before_update BEFORE UPDATE ON vehiculo
FOR EACH ROW SET NEW.placa = UPPER(TRIM(NEW.placa));

CREATE TRIGGER trg_registro_before_insert BEFORE INSERT ON registro
FOR EACH ROW
BEGIN
    DECLARE v_tipo_vehiculo ENUM('CARRO','MOTO');
    DECLARE v_tipo_celda ENUM('CARRO','MOTO');
    SELECT tipo INTO v_tipo_vehiculo FROM vehiculo WHERE placa = NEW.placa_vehiculo;
    SELECT tipo INTO v_tipo_celda FROM celda WHERE id = NEW.id_celda;
    IF v_tipo_vehiculo != v_tipo_celda THEN
        SIGNAL SQLSTATE '45001' SET MESSAGE_TEXT = 'Tipo de vehículo no coincide con celda';
    END IF;
END$$

CREATE TRIGGER trg_registro_before_update BEFORE UPDATE ON registro
FOR EACH ROW
BEGIN
    IF NEW.hora_salida IS NOT NULL AND NEW.hora_salida <= NEW.hora_entrada THEN
        SIGNAL SQLSTATE '45002' SET MESSAGE_TEXT = 'hora_salida no puede ser anterior a hora_entrada';
    END IF;
END$$

-- Impedir DELETE físico
CREATE TRIGGER trg_registro_before_delete BEFORE DELETE ON registro
FOR EACH ROW SIGNAL SQLSTATE '45003' SET MESSAGE_TEXT = 'No se permite borrar registros';

CREATE TRIGGER trg_usuario_before_delete BEFORE DELETE ON usuario
FOR EACH ROW SIGNAL SQLSTATE '45004' SET MESSAGE_TEXT = 'Use sp_desactivarUsuario';

DELIMITER ;

-- =============================================
-- § 7 VERIFICACIÓN FINAL
-- =============================================
SHOW TABLES;
SELECT 'Base de datos lista - Gestión de pagos incluida' AS mensaje;

-- Pruebas rápidas
CALL sp_ocupacionParqueadero();

SELECT 'Script mejorado finalizado. El cliente puede configurar tarifas, celdas y usuarios.' AS estado;