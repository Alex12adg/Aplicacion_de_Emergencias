# Aplicacion de Ayuda, Emergencias y Reservas

Aplicacion de escritorio en Java y JavaFX orientada a asistencia personal, gestion de emergencias y reserva de servicios de atencion. La interfaz centraliza login, dashboard, emergencias, alerta de peligro, monitor de salud, deteccion por voz, centros, historial medico, reservas y configuracion.

## Estado actual

La aplicacion ya permite:

- Iniciar sesion y registrar usuarios.
- Gestionar datos de cuenta desde configuracion.
- Lanzar emergencias manuales desde interfaz.
- Activar una alerta de peligro con escalado automatico.
- Simular deteccion por voz por palabra clave.
- Simular monitorizacion cardiaca y prealerta.
- Consultar centros de emergencia cercanos.
- Guardar historial medico y contactos de emergencia.
- Crear, consultar, editar y borrar reservas de atencion.

## Tecnologias

- Java
- JavaFX con FXML
- CSS
- JDBC
- MariaDB

## Punto de entrada

- `src/App/Main/Main.java`
- `src/GUI/AppGUI.java`

La aplicacion abre el login y, tras autenticacion, carga la vista principal con menu lateral y dashboard.

## Modulos principales

### Dashboard

- Acceso rapido a todos los modulos principales.
- Resumen visual inicial para el usuario autenticado.

Archivos:

- `src/GUI/Views/Home-view.fxml`
- `src/GUI/controllers/HomeController.java`
- `src/GUI/controllers/MainController.java`

### Emergencia

- Alta de emergencias manuales con tipo, gravedad y ubicacion.
- Presentacion del resultado y centros disponibles.

Archivos:

- `src/GUI/Views/Emergency-view.fxml`
- `src/GUI/controllers/EmergencyController.java`
- `src/Resources/Emergency/*`

### Alerta de peligro

- Modal bloqueante mientras la alerta esta activa.
- Escalado automatico a emergencia al agotar intentos.

Archivos:

- `src/GUI/Views/Danger-view.fxml`
- `src/GUI/controllers/DangerController.java`
- `src/Resources/Danger/*`

### Salud

- Simulacion de lecturas cardiacas.
- Prealerta por ausencia de pulso sostenida.

Archivos:

- `src/GUI/Views/health-view.fxml`
- `src/GUI/controllers/HealthController.java`
- `src/Resources/Heart/*`

### Voz

- Configuracion de palabra clave.
- Simulacion de escucha y disparo de emergencia.

Archivos:

- `src/GUI/Views/voice-view.fxml`
- `src/GUI/controllers/VoiceController.java`
- `src/Resources/Voice/*`

### Centros

- Carga de centros desde JSON local.
- Filtro por radio y ubicacion.
- Visualizacion de centros publicos de solo lectura.
- Alta, modificacion y borrado de centros privados del usuario.

Archivos:

- `src/GUI/Views/Centers-view.fxml`
- `src/GUI/controllers/CentersController.java`
- `src/Resources/Location/*`
- `src/Resources/Database/ReservationResourceDAO.java`

### Historial medico y contactos

- Guardado de alergias, condiciones y medicacion como listas por lineas.
- Alta y borrado de contactos de emergencia.

Archivos:

- `src/GUI/Views/Medical-form-view.fxml`
- `src/GUI/controllers/MedicalFormController.java`
- `src/Services/MedicalService.java`
- `src/Resources/Database/MedicalDAO.java`
- `src/Resources/Database/ContactDAO.java`

### Reservas

- Catalogo de recursos reservables sembrado en base de datos.
- Alta de reservas con validacion de fecha y tramo horario.
- Deteccion de solapes por recurso.
- Consulta de reservas del usuario.
- Borrado completo de reservas desde la interfaz.
- Edicion de reservas activas desde la interfaz.

Archivos:

- `src/GUI/Views/Reservations-view.fxml`
- `src/GUI/controllers/ReservationsController.java`
- `src/Services/ReservationService.java`
- `src/Resources/Database/ReservationDAO.java`
- `src/Resources/Database/ReservationResourceDAO.java`

### Configuracion

- Pantalla base funcional.
- Actualizacion de cuenta y eliminacion de cuenta segun la logica actual de servicios/DAO.

Archivos:

- `src/GUI/Views/Settings-view.fxml`
- `src/GUI/controllers/SettingsController.java`
- `src/Services/UserService.java`
- `src/Resources/Database/UserDAO.java`

## Base de datos

La conexion actual esta definida en `src/Resources/Database/DBConnection.java`:

- Base de datos: `emergency_app`
- Motor esperado: MariaDB
- URL: `jdbc:mariadb://localhost:3306/emergency_app`
- Usuario por defecto en desarrollo: `root`

### Tablas utilizadas por la aplicacion

- `users`
- `medical_info`
- `medical_allergies`
- `medical_conditions`
- `medical_medications`
- `contacts`
- `emergencies`
- `booking_resources`
- `bookings`

### Scripts disponibles

- `Docs/database/mariadb-full-schema.sql`: esquema completo de la aplicacion con tablas, claves foraneas e insercion base de recursos reservables.
- `Docs/database/migrate-medical-info-to-lists.sql`: migracion desde el formato medico antiguo con columnas `VARCHAR` al formato actual basado en listas.

### Importacion recomendada

1. Crear la base o ejecutar directamente el script completo.
2. Importar `Docs/database/mariadb-full-schema.sql`.
3. Verificar que el usuario y la clave definidos en `DBConnection.java` coinciden con el entorno local.

## Estructura del proyecto

```text
src/
  App/
    Main/
  GUI/
    Views/
    controllers/
    Styles/
  Resources/
    Contacts/
    Danger/
    Data/
    Database/
    Emergency/
    Heart/
    Location/
    Model/
    Notification/
    Session/
    User/
    Voice/
  Services/
Docs/
  API/
  database/
  README.md
```

## Ejecucion

Requisitos:

- JDK con JavaFX configurado.
- Driver MariaDB disponible en el entorno.
- Instancia MariaDB accesible con la configuracion indicada en `DBConnection.java`.

El proyecto esta organizado para ejecutarse desde IntelliJ IDEA con estructura basada en `src`.

## Notas

- El modulo de reservas inicializa y siembra su propio catalogo desde el codigo al cargarse.
- El resto de tablas de negocio se asumen existentes; por eso el esquema de referencia queda centralizado en `Docs/database/mariadb-full-schema.sql`.
- No hay una herramienta de build unificada en el repositorio.

## Autor

Proyecto de final de grado de DAM de Alejandro Mora.
