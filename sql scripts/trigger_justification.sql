CREATE OR REPLACE TRIGGER trg_audit_justification_update
AFTER UPDATE ON justifications
FOR EACH ROW
DECLARE
    v_employee_id NUMBER;
    v_employee_code VARCHAR2(50);
    v_employee_name VARCHAR2(200);
    v_modified_by VARCHAR2(100);
    v_text_changed BOOLEAN := FALSE;
    v_submitted_changed BOOLEAN := FALSE;
    v_approved_changed BOOLEAN := FALSE;
BEGIN
    -- Obtener información del empleado desde attendance_record
    SELECT 
        e.id_employee,
        e.employee_code,
        e.first_name || ' ' || e.last_name
    INTO 
        v_employee_id,
        v_employee_code,
        v_employee_name
    FROM employees e
    JOIN attendance_records ar ON ar.employee_id = e.id_employee
    WHERE ar.id_attendance_record = :NEW.attendance_record_id;
    
    -- Obtener usuario que realiza la modificación
    v_modified_by := SYS_CONTEXT('USERENV', 'SESSION_USER');
    
    -- Verificar si cambió el texto de justificación
    IF (:OLD.justification_text IS NULL AND :NEW.justification_text IS NOT NULL) OR
       (:OLD.justification_text IS NOT NULL AND :NEW.justification_text IS NULL) OR
       (:OLD.justification_text IS NOT NULL AND :NEW.justification_text IS NOT NULL AND 
        DBMS_LOB.COMPARE(:OLD.justification_text, :NEW.justification_text) != 0) THEN
        v_text_changed := TRUE;
    END IF;
    
    -- Verificar si cambió submitted_at
    IF (:OLD.submitted_at IS NULL AND :NEW.submitted_at IS NOT NULL) OR
       (:OLD.submitted_at IS NOT NULL AND :NEW.submitted_at IS NULL) OR
       (:OLD.submitted_at IS NOT NULL AND :NEW.submitted_at IS NOT NULL AND
        :OLD.submitted_at != :NEW.submitted_at) THEN
        v_submitted_changed := TRUE;
    END IF;
    
    -- Verificar si cambió approved
    IF (:OLD.approved IS NULL AND :NEW.approved IS NOT NULL) OR
       (:OLD.approved IS NOT NULL AND :NEW.approved IS NULL) OR
       (:OLD.approved IS NOT NULL AND :NEW.approved IS NOT NULL AND
        :OLD.approved != :NEW.approved) THEN
        v_approved_changed := TRUE;
    END IF;
    
    -- Insertar log solo si hubo cambios relevantes
    IF v_text_changed OR v_submitted_changed OR v_approved_changed THEN
        INSERT INTO justification_logs (
            id_justification,
            id_attendance_record,
            employee_id,
            employee_code,
            employee_name,
            action_type,
            old_justification_text,
            new_justification_text,
            old_submitted_at,
            new_submitted_at,
            old_approved,
            new_approved,
            modified_by,
            modified_at,
            ip_address
        ) VALUES (
            :NEW.id_justification,
            :NEW.attendance_record_id,
            v_employee_id,
            v_employee_code,
            v_employee_name,
            'UPDATE',
            :OLD.justification_text,
            :NEW.justification_text,
            :OLD.submitted_at,
            :NEW.submitted_at,
            :OLD.approved,
            :NEW.approved,
            v_modified_by,
            CURRENT_TIMESTAMP,
            SYS_CONTEXT('USERENV', 'IP_ADDRESS')
        );
    END IF;
    
EXCEPTION
    WHEN OTHERS THEN
        RAISE_APPLICATION_ERROR(-20003, 'Error en trigger de auditoría de justificación UPDATE: ' || SQLERRM);
END;
/