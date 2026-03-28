-- =============================================
-- Script  : parqueadero_db_ENTREGA_FINAL.sql
-- Proyecto: Parqueadero Autos Colombia
-- Equipo  : Cadavid · Paternina · Marin
-- Fecha   : 2026
-- =============================================
-- INSTRUCCIONES:
--   1. Abrir en MySQL Workbench
--   2. Ejecutar TODO con Ctrl+Shift+Enter
--   3. Verificar que el output muestre solo OK
--   4. Arrancar la app Java — el sistema hace el resto
-- =============================================

-- ── 0. Crear base de datos ───────────────────
DROP DATABASE IF EXISTS parqueadero_db;
CREATE DATABASE parqueadero_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE parqueadero_db;

-- ── 1. Tabla usuario ─────────────────────────
-- Soporta It.1 (login) e It.2 (clientes)
-- documento NULL: permite admin sin documento
-- activo: borrado lógico RF13 — nunca DELETE
-- ── 1. Tabla usuario ─────────────────────────
CREATE TABLE usuario (
    id             INT          AUTO_INCREMENT PRIMARY KEY,
    nombre         VARCHAR(50)  NOT NULL UNIQUE
                   COMMENT 'Nombre de acceso al sistema',
    rol            ENUM('vigilante','admin')
                   NOT NULL DEFAULT 'vigilante',
    password       VARCHAR(255) NOT NULL
                   COMMENT 'Hash SHA2-256 — nunca texto plano (RNF06)',
    documento      VARCHAR(20)  NULL UNIQUE
                   COMMENT 'RF14 — NULL para cuentas técnicas',
    telefono       VARCHAR(15)  NULL,
    correo         VARCHAR(100) NULL,
    tipo_cliente   ENUM('mensual','visitante') NULL DEFAULT NULL
                   COMMENT 'NULL para Mensual/visitante',
    activo         BOOLEAN      NOT NULL DEFAULT TRUE
                   COMMENT 'RF13 — FALSE=desactivado (reversible)',

    fecha_registro DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
                   COMMENT 'Automática — Java usa LocalDateTime.now()'
);

CREATE INDEX idx_usuario_nombre    ON usuario(nombre);
CREATE INDEX idx_usuario_documento ON usuario(documento);
CREATE INDEX idx_usuario_activo    ON usuario(activo);

-- ── 2. Tabla vehiculo ────────────────────────
-- Java inserta aquí con guardarOActualizar()
-- al registrar la primera entrada de cada placa
CREATE TABLE vehiculo (
    placa VARCHAR(10)          PRIMARY KEY
          COMMENT 'Mayúsculas siempre — Java aplica toUpperCase()',
    tipo  ENUM('CARRO','MOTO') NOT NULL
);

-- ── 3. Tabla celda ───────────────────────────
-- Java lee con buscarCeldaDisponible()
-- Java actualiza con actualizarDisponibilidad()
CREATE TABLE celda (
    id         INT  AUTO_INCREMENT PRIMARY KEY,
    tipo       ENUM('CARRO','MOTO') NOT NULL,
    disponible BOOLEAN DEFAULT TRUE
               COMMENT 'TRUE=libre · FALSE=ocupada'
);

CREATE INDEX idx_celda_tipo_disponible
    ON celda(tipo, disponible);

-- ── 4. Tabla registro ────────────────────────
-- Java inserta con registrarEntrada()
-- Java actualiza hora_salida con registrarSalida()
-- hora_salida NULL = vehículo dentro del parqueadero
CREATE TABLE registro (
    id             INT      AUTO_INCREMENT PRIMARY KEY,
    hora_entrada   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
                   COMMENT 'Automática — RF09',
    hora_salida    DATETIME NULL
                   COMMENT 'NULL mientras está dentro — RF04',
    placa_vehiculo VARCHAR(10) NOT NULL,
    id_celda       INT         NOT NULL,
    id_usuario     INT         NOT NULL,

    FOREIGN KEY (placa_vehiculo)
        REFERENCES vehiculo(placa) ON DELETE CASCADE,
    FOREIGN KEY (id_celda)
        REFERENCES celda(id)       ON DELETE RESTRICT,
    FOREIGN KEY (id_usuario)
        REFERENCES usuario(id)     ON DELETE RESTRICT,

    INDEX idx_placa           (placa_vehiculo),
    INDEX idx_entrada_abierta (hora_salida)
);

-- ── 5. Tabla asignacion_celda ────────────────
-- Java inserta con AsignacionDAO.asignar()
-- Java desactiva con AsignacionDAO.desactivar()
-- activa=FALSE libera celda sin borrar historial
CREATE TABLE asignacion_celda (
    id               INT  AUTO_INCREMENT PRIMARY KEY,
    id_usuario       INT  NOT NULL,
    id_celda         INT  NOT NULL,
    fecha_asignacion DATE NOT NULL DEFAULT (CURRENT_DATE),
    activa           BOOLEAN       DEFAULT TRUE
                     COMMENT 'FALSE = liberada',

    FOREIGN KEY (id_usuario)
        REFERENCES usuario(id) ON DELETE CASCADE,
    FOREIGN KEY (id_celda)
        REFERENCES celda(id)   ON DELETE RESTRICT,

    UNIQUE KEY uq_celda_activa (id_celda, activa),
    INDEX idx_asignacion_activa (id_usuario, activa)
);

-- ── 6. Vista — vehículos activos ─────────────
-- Usada por getVehiculosActivos() (RF07)
CREATE VIEW v_vehiculos_activos AS
SELECT
    r.id,
    r.placa_vehiculo,
    r.id_celda,
    r.hora_entrada,
    TIMESTAMPDIFF(MINUTE, r.hora_entrada, NOW())
        AS minutos_permanencia,
    c.tipo     AS tipo_celda,
    u.nombre   AS registrado_por
FROM  registro r
JOIN  celda    c ON r.id_celda   = c.id
JOIN  usuario  u ON r.id_usuario = u.id
WHERE r.hora_salida IS NULL;

-- ════════════════════════════════════════════
-- DATOS MÍNIMOS DE ARRANQUE
-- Solo lo necesario para que Java funcione
-- El resto lo genera la app en tiempo real
-- ════════════════════════════════════════════

-- ── Admin del sistema ────────────────────────
-- Contraseña: Admin_2026
-- Sin documento (cuenta técnica, no cliente)
INSERT INTO usuario (nombre, rol, password)
VALUES ('admin', 'admin', SHA2('Admin_2026', 256));
SELECT * FROM usuario 
WHERE activo = TRUE 
AND rol != 'admin'; -- Esto oculta al admin de la lista

-- ── 20 celdas disponibles ────────────────────
-- 10 CARRO + 10 MOTO
-- Java las asigna automáticamente al registrar entrada
INSERT INTO celda (tipo, disponible) VALUES
    ('CARRO',TRUE),('CARRO',TRUE),('CARRO',TRUE),
    ('CARRO',TRUE),('CARRO',TRUE),('CARRO',TRUE),
    ('CARRO',TRUE),('CARRO',TRUE),('CARRO',TRUE),
    ('CARRO',TRUE),
    ('MOTO', TRUE),('MOTO', TRUE),('MOTO', TRUE),
    ('MOTO', TRUE),('MOTO', TRUE),('MOTO', TRUE),
    ('MOTO', TRUE),('MOTO', TRUE),('MOTO', TRUE),
    ('MOTO', TRUE);


-- ==============================
-- ── 1. Usuarios Clientes ──────────────────────
-- Se insertan con su respectivo tipo_cliente (RF14)
INSERT INTO usuario (nombre, documento, telefono, correo, tipo_cliente, password) 
VALUES 
-- Clientes de Carros (5)
('Juan Perez', '10102020', '3001112233', 'juan@mail.com', 'mensual', SHA2('pass123', 256)),
('Maria Lopez', '20203030', '3104445566', 'maria@mail.com', 'visitante', SHA2('pass123', 256)),
('Carlos Ruiz', '40405050', '3207778899', 'carlos@mail.com', 'mensual', SHA2('pass123', 256)),
('Ana Gomez', '50506060', '3151234567', 'ana@mail.com', 'visitante', SHA2('pass123', 256)),
('Luis Castro', '70708080', '3019876543', 'luis@mail.com', 'mensual', SHA2('pass123', 256)),
-- Clientes de Motos (3)
('Pedro Marmol', '80809090', '3000001111', 'pedro@mail.com', 'visitante', SHA2('pass123', 256)),
('Sara Villa', '90901111', '3122223333', 'sara@mail.com', 'mensual', SHA2('pass123', 256)),
('Ramiro Paz', '12121313', '3189998877', 'ramiro@mail.com', 'visitante', SHA2('pass123', 256));

-- ── 2. Vehículos ──────────────────────────────
INSERT INTO vehiculo (placa, tipo) 
VALUES 
('ABC123', 'CARRO'), ('DEF456', 'CARRO'), ('GHI789', 'CARRO'), ('JKL012', 'CARRO'), ('MNO345', 'CARRO'),
('XYZ78C', 'MOTO'), ('QWE12D', 'MOTO'), ('ASD45E', 'MOTO');

-- ── 3. Registros de Entrada (Activos) ─────────
-- Vinculamos la placa con la celda y el usuario correspondiente
-- Carros ocupan celdas 1 a 5
INSERT INTO registro (placa_vehiculo, id_celda, id_usuario, hora_entrada) VALUES 
('ABC123', 1, 2, DATE_SUB(NOW(), INTERVAL 2 HOUR)),
('DEF456', 2, 3, DATE_SUB(NOW(), INTERVAL 1 HOUR)),
('GHI789', 3, 4, DATE_SUB(NOW(), INTERVAL 30 MINUTE)),
('JKL012', 4, 5, DATE_SUB(NOW(), INTERVAL 5 HOUR)),
('MNO345', 5, 6, DATE_SUB(NOW(), INTERVAL 15 MINUTE));

-- Motos ocupan celdas 11 a 13
INSERT INTO registro (placa_vehiculo, id_celda, id_usuario, hora_entrada) VALUES 
('XYZ78C', 11, 7, DATE_SUB(NOW(), INTERVAL 3 HOUR)),
('QWE12D', 12, 8, DATE_SUB(NOW(), INTERVAL 45 MINUTE)),
('ASD45E', 13, 9, DATE_SUB(NOW(), INTERVAL 10 MINUTE));

-- ── 4. Sincronización de Celdas ────────────────
-- Marcamos como ocupadas para que Java no las asigne de nuevo
UPDATE celda SET disponible = FALSE WHERE id IN (1, 2, 3, 4, 5, 11, 12, 13);

-- ════════════════════════════════════════════
-- VERIFICACIÓN FINAL
-- Todos deben retornar datos correctos
-- ════════════════════════════════════════════

-- Admin creado correctamente
SELECT id, nombre, tipo_cliente
FROM   usuario;

-- 20 celdas listas (10 CARRO + 10 MOTO)
SELECT tipo, COUNT(*) AS total
FROM   celda
GROUP  BY tipo;

-- ====================================ERRORES Y EJECUCIONES NUEVAS
SET SQL_SAFE_UPDATES = 0;
UPDATE usuario SET tipo_cliente = 'visitante' WHERE tipo_cliente IS NULL;
SET SQL_SAFE_UPDATES = 1;
-- DELETE FROM registro WHERE id_usuario >= 23;
-- DELETE FROM usuario WHERE id >= 23;
UPDATE usuario SET tipo_cliente = 'visitante' 
WHERE tipo_cliente IS NULL OR tipo_cliente = '' OR tipo_cliente = ' ';
ALTER TABLE usuario MODIFY COLUMN tipo_cliente VARCHAR(50) NOT NULL;
SHOW TABLES;



SELECT u.nombre, COUNT(r.id) as total_celdas
FROM usuario u
JOIN registro r ON u.id = r.id_usuario
WHERE r.hora_salida IS NULL
GROUP BY u.id, u.nombre
HAVING COUNT(r.id) > 1;

-- Estructura lista para Java
SELECT 'BD lista para la app Java' AS estado;