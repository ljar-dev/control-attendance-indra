package com.indra.attendance_control.dtos.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceGeneralReportDto {
    
    // Chart 1: Estado de asistencias
    private AttendanceStatusStats attendanceStats;
    
    // Chart 2: Justificaciones
    private JustificationStats justificationStats;
    
    // Información del rango de fechas
    private String startDate;
    private String endDate;
    private Integer totalEmployees;
    private Integer totalRecords;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AttendanceStatusStats {
        private Long onTime;        // Puntuales (ON_TIME)
        private Long late;          // Tarde (LATE)
        private Long absent;        // Ausencias (ABSENT)
        private Long earlyDeparture; // Salida temprana (EARLY_DEPARTURE)
        
        // Porcentajes calculados
        private Double onTimePercentage;
        private Double latePercentage;
        private Double absentPercentage;
        private Double earlyDeparturePercentage;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JustificationStats {
        private Long totalRequired;      // Total que requieren justificación
        private Long justified;          // Justificadas (con texto)
        private Long notJustified;       // No justificadas (sin texto)
        
        // Porcentajes
        private Double justifiedPercentage;
        private Double notJustifiedPercentage;
    }
}