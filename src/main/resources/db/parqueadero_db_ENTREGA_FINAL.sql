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
                   COMMENT 'NULL para admin/vigilante',
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

-- ════════════════════════════════════════════
-- VERIFICACIÓN FINAL
-- Todos deben retornar datos correctos
-- ════════════════════════════════════════════

-- Admin creado correctamente
SELECT id, nombre, rol
FROM   usuario;

-- 20 celdas listas (10 CARRO + 10 MOTO)
SELECT tipo, COUNT(*) AS total
FROM   celda
GROUP  BY tipo;

-- Estructura lista para Java
SELECT 'BD lista para la app Java' AS estado;