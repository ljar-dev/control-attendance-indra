-- ============================================================================
-- DATOS DE PRUEBA - SISTEMA DE CONTROL DE ASISTENCIA
-- Generado para: Oracle 23ai
-- Fecha: Diciembre 2025
-- ============================================================================

INSERT INTO roles (name) VALUES ('ADMIN');
INSERT INTO roles (name) VALUES ('EMPLOYEE');

-- ============================================================================
-- 1. USUARIOS (6 USUARIOS TOTALES)
-- ============================================================================
-- Contraseña para todos: "$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O"
INSERT INTO users (username, password, enabled, must_change_password) 
VALUES ('rsantos', '$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O', 1, 0); -- ID: 1

INSERT INTO users (username, password, enabled, must_change_password) 
VALUES ('pmorales', '$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O', 1, 0); -- ID: 2

INSERT INTO users (username, password, enabled, must_change_password) 
VALUES ('pperez', '$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O', 1, 1); -- ID: 3

INSERT INTO users (username, password, enabled, must_change_password) 
VALUES ('mrodriguez', '$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O', 1, 1); -- ID: 4

INSERT INTO users (username, password, enabled, must_change_password) 
VALUES ('ggomez', '$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O', 1, 1); -- ID: 5

INSERT INTO users (username, password, enabled, must_change_password) 
VALUES ('afernandez', '$2a$10$2Z.k1KcSYVYcL3im53Il6OdeOFj4DJxuZYKSDV1htSnImDklApB7O', 1, 1); -- ID: 6

-- ============================================================================
-- 2. ASIGNACIÓN DE ROLES
-- ============================================================================
-- Admins (también tendrán rol de empleado porque son empleados)
INSERT INTO user_roles (user_id, role_id) VALUES (1, 1); -- admin1 -> ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (1, 2); -- admin1 -> ROLE_EMPLOYEE
INSERT INTO user_roles (user_id, role_id) VALUES (2, 1); -- admin2 -> ROLE_ADMIN
INSERT INTO user_roles (user_id, role_id) VALUES (2, 2); -- admin2 -> ROLE_EMPLOYEE

-- Empleados regulares
INSERT INTO user_roles (user_id, role_id) VALUES (3, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (4, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (5, 2);
INSERT INTO user_roles (user_id, role_id) VALUES (6, 2);

-- ============================================================================
-- 3. EMPLEADOS (6 PERSONAS EN TOTAL: 2 ADMINS + 4 EMPLEADOS)
-- ============================================================================
-- Admin 1 - También es empleado
INSERT INTO employees (first_name, last_name, employee_code, department, position, hire_date, user_id, enabled, first_login)
VALUES ('Roberto Renato', 'Santos Vargas', 'ADM001', 'Administracion', 'Gerente General', DATE '2022-01-10', 1, 1, 0);

-- Admin 2 - También es empleado
INSERT INTO employees (first_name, last_name, employee_code, department, position, hire_date, user_id, enabled, first_login)
VALUES ('Patricia Elena', 'Morales Diaz', 'ADM002', 'Recursos Humanos', 'Jefe de RRHH', DATE '2022-02-15', 2, 1, 0);

-- Empleado 1
INSERT INTO employees (first_name, last_name, employee_code, department, position, hire_date, user_id, enabled, first_login)
VALUES ('Pedro', 'Perez Gomez', 'EMP001', 'Desarrollo', 'Desarrollador Senior', DATE '2023-01-15', 3, 1, 0);

-- Empleado 2
INSERT INTO employees (first_name, last_name, employee_code, department, position, hire_date, user_id, enabled, first_login)
VALUES ('Maria Elena', 'Rodriguez Silva', 'EMP002', 'Desarrollo', 'Desarrolladora Junior', DATE '2023-03-20', 4, 1, 0);

-- Empleado 3
INSERT INTO employees (first_name, last_name, employee_code, department, position, hire_date, user_id, enabled, first_login)
VALUES ('Gabriela', 'Gomez Martinez', 'EMP003', 'Finanzas', 'Analista Financiero', DATE '2023-02-10', 5, 1, 0);

-- Empleado 4
INSERT INTO employees (first_name, last_name, employee_code, department, position, hire_date, user_id, enabled, first_login)
VALUES ('Ana Sofia', 'Fernandez Torres', 'EMP004', 'Marketing', 'Diseñadora Grafica', DATE '2023-04-05', 6, 1, 0);

-- ============================================================================
-- 4. HORARIOS DE TRABAJO (Lunes a Viernes, 8:00 AM - 5:00 PM)
-- ============================================================================
-- Empleado 1: Roberto Carlos Santos (Admin)
INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (1, 'MONDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (1, 'TUESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (1, 'WEDNESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (1, 'THURSDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (1, 'FRIDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

-- Empleado 2: Patricia Elena Morales (Admin)
INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (2, 'MONDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (2, 'TUESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (2, 'WEDNESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (2, 'THURSDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (2, 'FRIDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

-- Empleado 3: Juan Carlos Perez
INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (3, 'MONDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (3, 'TUESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (3, 'WEDNESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (3, 'THURSDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (3, 'FRIDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

-- Empleado 4: Maria Elena Rodriguez
INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (4, 'MONDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (4, 'TUESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (4, 'WEDNESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (4, 'THURSDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (4, 'FRIDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

-- Empleado 5: Carlos Alberto Gomez
INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (5, 'MONDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (5, 'TUESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (5, 'WEDNESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (5, 'THURSDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (5, 'FRIDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

-- Empleado 6: Ana Sofia Fernandez
INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (6, 'MONDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (6, 'TUESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (6, 'WEDNESDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (6, 'THURSDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

INSERT INTO work_schedules (employee_id, day_of_week, start_time, end_time, enabled)
VALUES (6, 'FRIDAY', TIMESTAMP '2024-01-01 08:00:00', TIMESTAMP '2024-01-01 17:00:00', 1);

-- ============================================================================
-- 5. REGISTROS DE ASISTENCIA - DEL 10 AL 14 DE DICIEMBRE 2025
-- ============================================================================

-- MARTES 10 DE DICIEMBRE 2025
-- Empleado 1 (Roberto - Admin): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (1, TIMESTAMP '2025-12-10 07:55:00', TIMESTAMP '2025-12-10 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-10 00:00:00');

-- Empleado 2 (Patricia - Admin): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (2, TIMESTAMP '2025-12-10 08:00:00', TIMESTAMP '2025-12-10 17:05:00', 'ON_TIME', TIMESTAMP '2025-12-10 00:00:00');

-- Empleado 3 (Juan): Tarde
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (3, TIMESTAMP '2025-12-10 08:25:00', TIMESTAMP '2025-12-10 17:00:00', 'LATE', TIMESTAMP '2025-12-10 00:00:00');

-- Empleado 4 (Maria): Ausente
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (4, NULL, NULL, 'ABSENT', TIMESTAMP '2025-12-10 00:00:00');

-- Empleado 5 (Carlos): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (5, TIMESTAMP '2025-12-10 08:00:00', TIMESTAMP '2025-12-10 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-10 00:00:00');

-- Empleado 6 (Ana): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (6, TIMESTAMP '2025-12-10 08:00:00', TIMESTAMP '2025-12-10 17:03:00', 'ON_TIME', TIMESTAMP '2025-12-10 00:00:00');

-- MIÉRCOLES 11 DE DICIEMBRE 2025
-- Empleado 1 (Roberto - Admin): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (1, TIMESTAMP '2025-12-11 08:00:00', TIMESTAMP '2025-12-11 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-11 00:00:00');

-- Empleado 2 (Patricia - Admin): Tarde
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (2, TIMESTAMP '2025-12-11 08:30:00', TIMESTAMP '2025-12-11 17:00:00', 'LATE', TIMESTAMP '2025-12-11 00:00:00');

-- Empleado 3 (Juan): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (3, TIMESTAMP '2025-12-11 08:00:00', TIMESTAMP '2025-12-11 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-11 00:00:00');

-- Empleado 4 (Maria): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (4, TIMESTAMP '2025-12-11 08:00:00', TIMESTAMP '2025-12-11 17:05:00', 'ON_TIME', TIMESTAMP '2025-12-11 00:00:00');

-- Empleado 5 (Carlos): Salida temprana
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (5, TIMESTAMP '2025-12-11 08:00:00', TIMESTAMP '2025-12-11 15:30:00', 'EARLY_DEPARTURE', TIMESTAMP '2025-12-11 00:00:00');

-- Empleado 6 (Ana): Permiso
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (6, NULL, NULL, 'PERMISSION', TIMESTAMP '2025-12-11 00:00:00');

-- JUEVES 12 DE DICIEMBRE 2025
-- Empleado 1 (Roberto - Admin): Tarde
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (1, TIMESTAMP '2025-12-12 08:20:00', TIMESTAMP '2025-12-12 17:00:00', 'LATE', TIMESTAMP '2025-12-12 00:00:00');

-- Empleado 2 (Patricia - Admin): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (2, TIMESTAMP '2025-12-12 08:00:00', TIMESTAMP '2025-12-12 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-12 00:00:00');

-- Empleado 3 (Juan): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (3, TIMESTAMP '2025-12-12 08:00:00', TIMESTAMP '2025-12-12 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-12 00:00:00');

-- Empleado 4 (Maria): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (4, TIMESTAMP '2025-12-12 08:00:00', TIMESTAMP '2025-12-12 17:02:00', 'ON_TIME', TIMESTAMP '2025-12-12 00:00:00');

-- Empleado 5 (Carlos): Licencia médica
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (5, NULL, NULL, 'MEDICAL_LEAVE', TIMESTAMP '2025-12-12 00:00:00');

-- Empleado 6 (Ana): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (6, TIMESTAMP '2025-12-12 08:00:00', TIMESTAMP '2025-12-12 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-12 00:00:00');

-- VIERNES 13 DE DICIEMBRE 2025
-- Empleado 1 (Roberto - Admin): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (1, TIMESTAMP '2025-12-13 08:00:00', TIMESTAMP '2025-12-13 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-13 00:00:00');

-- Empleado 2 (Patricia - Admin): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (2, TIMESTAMP '2025-12-13 08:00:00', TIMESTAMP '2025-12-13 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-13 00:00:00');

-- Empleado 3 (Juan): Tarde
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (3, TIMESTAMP '2025-12-13 08:35:00', TIMESTAMP '2025-12-13 17:10:00', 'LATE', TIMESTAMP '2025-12-13 00:00:00');

-- Empleado 4 (Maria): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (4, TIMESTAMP '2025-12-13 08:00:00', TIMESTAMP '2025-12-13 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-13 00:00:00');

-- Empleado 5 (Carlos): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (5, TIMESTAMP '2025-12-13 08:00:00', TIMESTAMP '2025-12-13 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-13 00:00:00');

-- Empleado 6 (Ana): A tiempo
INSERT INTO attendance_records (employee_id, check_in, check_out, status, created_at)
VALUES (6, TIMESTAMP '2025-12-13 08:00:00', TIMESTAMP '2025-12-13 17:00:00', 'ON_TIME', TIMESTAMP '2025-12-13 00:00:00');

-- SÁBADO 14 DE DICIEMBRE 2025 (fin de semana - sin registros normalmente)
-- Sin registros porque es sábado y solo trabajan lunes a viernes

-- ============================================================================
-- 6. JUSTIFICACIONES PARA REGISTROS DEL 10-14 DE DICIEMBRE
-- ============================================================================

-- Justificación APROBADA para la tardanza de Juan (Empleado 3) el 10 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (3, 'Tuve problemas con el transporte publico. Hubo un accidente en la avenida principal que retraso mi llegada.', 
        TIMESTAMP '2025-12-10 09:00:00', 1, TIMESTAMP '2025-12-10 09:00:00', TIMESTAMP '2025-12-10 09:00:00');

-- Justificación PENDIENTE para la ausencia de Maria (Empleado 4) el 10 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (4, NULL, NULL, NULL, TIMESTAMP '2025-12-11 00:01:00', TIMESTAMP '2025-12-11 00:01:00');

-- Justificación APROBADA para la tardanza de Patricia Admin (Empleado 2) el 11 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (8, 'Problemas con el vehiculo. La bateria se descargo y tuve que llamar a asistencia en carretera.', 
        TIMESTAMP '2025-12-11 09:15:00', 1, TIMESTAMP '2025-12-11 09:15:00', TIMESTAMP '2025-12-11 09:15:00');

-- Justificación APROBADA para la salida temprana de Carlos (Empleado 5) el 11 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (11, 'Cita medica programada con anticipacion. Presente el comprobante a Recursos Humanos.', 
        TIMESTAMP '2025-12-11 14:00:00', 1, TIMESTAMP '2025-12-11 14:00:00', TIMESTAMP '2025-12-11 14:00:00');

-- Justificación APROBADA para el permiso de Ana (Empleado 6) el 11 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (12, 'Solicite permiso con anticipacion para realizar tramites bancarios urgentes.', 
        TIMESTAMP '2025-12-10 15:00:00', 1, TIMESTAMP '2025-12-10 15:00:00', TIMESTAMP '2025-12-10 15:00:00');

-- Justificación RECHAZADA para la tardanza de Roberto Admin (Empleado 1) el 12 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (13, 'Me quede dormido porque la alarma no sono.', 
        TIMESTAMP '2025-12-12 08:45:00', 0, TIMESTAMP '2025-12-12 08:45:00', TIMESTAMP '2025-12-12 08:45:00');

-- Justificación APROBADA para la licencia médica de Carlos (Empleado 5) el 12 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (17, 'Certificado medico adjunto. Diagnostico: gripe fuerte con fiebre alta. Reposo recomendado de 24 horas.', 
        TIMESTAMP '2025-12-12 10:00:00', 1, TIMESTAMP '2025-12-12 10:00:00', TIMESTAMP '2025-12-12 10:00:00');

-- Justificación RECHAZADA para la tardanza de Juan (Empleado 3) el 13 de diciembre
INSERT INTO justifications (attendance_record_id, justification_text, submitted_at, approved, created_at, updated_at)
VALUES (21, 'Habia mucho trafico en la carretera.', 
        TIMESTAMP '2025-12-13 09:00:00', 0, TIMESTAMP '2025-12-13 09:00:00', TIMESTAMP '2025-12-13 09:00:00');

COMMIT;
