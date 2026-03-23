# Autos Colombia - Sistema de Gestión de Parqueadero

Sistema integral desarrollado en **Java 21** con **Maven** 
y **MySQL** para la administración de celdas, usuarios mensuales y visitantes.

## 🚀 Tecnologías
* **Backend:** Java (JDBC).
* **Gestor de Dependencias:** Maven.
* **Base de Datos:** MySQL 8.0.46
* **Interfaz Gráfica:** Swing (JFrame, JPanel, CardLayout).

## 🛠️ Estructura del Proyecto
* `dao`: Capa de persistencia (SQL).
* `service`: Lógica de negocio y validaciones.
* `ui`: Interfaz gráfica de usuario.
* `model`: Clases de entidad.

## 📌 Funcionalidades Principales
* Mapa de celdas en tiempo real (Sincronizado con base de datos).
* Registro de entradas/salidas con cálculo de tiempo.
* Gestión de usuarios mensuales y asignación de celdas.
* Auto-liberación de celdas para evitar duplicados.
