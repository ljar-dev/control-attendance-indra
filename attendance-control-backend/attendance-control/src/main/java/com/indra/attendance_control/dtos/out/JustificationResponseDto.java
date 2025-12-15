package com.indra.attendance_control.dtos.out;

import com.indra.attendance_control.models.enums.AttendanceStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JustificationResponseDto {
    
    // Información del empleado
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    
    // Información del attendance record
    private Long attendanceRecordId;
    private String attendanceDate;
    private String checkIn;
    private String checkOut;
    private AttendanceStatus attendanceStatus;
    
    // Información de la justificación
    private Long idJustification;
    private String justificationText;
    private String submittedAt;
    
    // Timestamps
    private String createdAt;
    private String updatedAt;
}