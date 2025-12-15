package com.indra.attendance_control.models.enums;

public enum AttendanceStatus {
    ON_TIME("A Tiempo"),
    LATE("Tardanza"),
    ABSENT("Ausente"),
    EARLY_DEPARTURE("Salida Temprana"),
    JUSTIFIED_ABSENCE("Ausencia Justificada"),
    MEDICAL_LEAVE("Licencia Médica"),
    VACATION("Vacaciones"),
    PERMISSION("Permiso");

    private final String displayName;

    AttendanceStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}