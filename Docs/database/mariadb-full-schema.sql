CREATE DATABASE IF NOT EXISTS emergency_app
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_general_ci;

USE emergency_app;

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    role VARCHAR(30) NOT NULL DEFAULT 'user',
    email VARCHAR(150) NOT NULL,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_users_email (email)
);

CREATE TABLE IF NOT EXISTS medical_info (
    user_id INT PRIMARY KEY,
    allergies VARCHAR(255),
    conditions VARCHAR(255),
    medications VARCHAR(255),
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_medical_info_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS contacts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    contact_name VARCHAR(120) NOT NULL,
    contact_phone VARCHAR(30) NOT NULL,
    relation VARCHAR(80),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_contacts_user (user_id),
    CONSTRAINT fk_contacts_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS emergencies (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    type VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_emergencies_user (user_id),
    CONSTRAINT fk_emergencies_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS booking_resources (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    category VARCHAR(80) NOT NULL,
    location VARCHAR(120) NOT NULL,
    description VARCHAR(255),
    slot_duration_minutes INT NOT NULL DEFAULT 30,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    public_resource BOOLEAN NOT NULL DEFAULT TRUE,
    owner_user_id INT NULL,
    KEY idx_booking_resources_owner (owner_user_id),
    CONSTRAINT fk_booking_resources_owner
        FOREIGN KEY (owner_user_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_id INT NOT NULL,
    resource_id INT NOT NULL,
    appointment_date DATE NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    purpose VARCHAR(120) NOT NULL,
    notes VARCHAR(255),
    status VARCHAR(30) NOT NULL DEFAULT 'ACTIVA',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    KEY idx_bookings_user (user_id),
    KEY idx_bookings_resource_date (resource_id, appointment_date),
    CONSTRAINT fk_bookings_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT fk_bookings_resource
        FOREIGN KEY (resource_id) REFERENCES booking_resources(id)
);

INSERT INTO booking_resources (name, category, location, description, slot_duration_minutes, active, public_resource, owner_user_id)
VALUES
    ('Consulta de medicina general', 'Cita medica', 'Centro de salud Norte', 'Revision general, recetas y seguimiento de sintomas.', 30, TRUE, TRUE, NULL),
    ('Pediatria', 'Cita medica', 'Centro de salud Infantil', 'Atencion pediatrica para menores y controles basicos.', 30, TRUE, TRUE, NULL),
    ('Psicologia clinica', 'Cita medica', 'Centro integral de bienestar', 'Atencion psicologica y seguimiento emocional.', 45, TRUE, TRUE, NULL),
    ('Atencion en comisaria', 'Comisaria', 'Comisaria Centro', 'Tramites, denuncias y consultas presenciales.', 20, TRUE, TRUE, NULL),
    ('Renovacion de documentacion', 'Comisaria', 'Comisaria Distrito Norte', 'Reserva para tramites administrativos y documentacion.', 20, TRUE, TRUE, NULL),
    ('Trabajo social', 'Atencion social', 'Oficina municipal', 'Orientacion social y derivacion de servicios.', 40, TRUE, TRUE, NULL);
