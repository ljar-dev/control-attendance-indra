package com.indra.attendance_control.models.enums;

public enum JustificationStatus {
    PENDING("Pendiente"),           // Creado por batch, empleado no ha justificado
    SUBMITTED("Enviado"),           // Empleado envió justificación
    APPROVED("Aprobado"),           // Admin aprobó la justificación
    REJECTED("Rechazado");          // Admin rechazó la justificación
    
    private final String displayName;
    
    JustificationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}