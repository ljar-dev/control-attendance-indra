CREATE OR REPLACE TRIGGER trg_audit_attendance_record
AFTER UPDATE ON attendance_records
FOR EACH ROW
DECLARE
    v_employee_code VARCHAR2(50);
    v_employee_name VARCHAR2(200);
    v_modified_by VARCHAR2(100);
BEGIN
    -- Obtener información del empleado
    SELECT e.employee_code, e.first_name || ' ' || e.last_name
    INTO v_employee_code, v_employee_name
    FROM employees e
    WHERE e.id_employee = :NEW.employee_id;
    
    -- Obtener usuario que realiza la modificación
    v_modified_by := SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    -- Insertar log solo si hubo cambios relevantes
    IF (NVL(:OLD.check_in, TO_TIMESTAMP('1900-01-01', 'YYYY-MM-DD')) != NVL(:NEW.check_in, TO_TIMESTAMP('1900-01-01', 'YYYY-MM-DD'))) OR
       (NVL(:OLD.check_out, TO_TIMESTAMP('1900-01-01', 'YYYY-MM-DD')) != NVL(:NEW.check_out, TO_TIMESTAMP('1900-01-01', 'YYYY-MM-DD'))) OR
       (NVL(:OLD.status, 'NONE') != NVL(:NEW.status, 'NONE')) THEN
        
        INSERT INTO attendance_record_logs (
            id_attendance_record,
            employee_id,
            employee_code,
            employee_name,
            action_type,
            old_check_in,
            new_check_in,
            old_check_out,
            new_check_out,
            old_status,
            new_status,
            modified_by,
            modified_at,
            ip_address
        ) VALUES (
            :NEW.id_attendance_record,
            :NEW.employee_id,
            v_employee_code,
            v_employee_name,
            'UPDATE',
            :OLD.check_in,
            :NEW.check_in,
            :OLD.check_out,
            :NEW.check_out,
            :OLD.status,
            :NEW.status,
            v_modified_by,
            CURRENT_TIMESTAMP,
            SYS_CONTEXT('USERENV', 'IP_ADDRESS')
        );
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20001, 'Error en trigger de auditoría: ' || SQLERRM);
END;
/
