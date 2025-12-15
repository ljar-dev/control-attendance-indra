package com.indra.attendance_control.dtos.out;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmployeeResponseDto {
    private Long idEmployee;
    private String firstName;
    private String lastName;
    private String employeeCode; // E0001
    private String department;
    private String position;
    private LocalDate hireDate;
    private boolean active;
    
    // Datos del usuario
    private Long userId;
    private String username; // jdoe
    private String temporaryPassword; // Solo se incluye en la creación
    private boolean mustChangePassword;
    
    // Horarios
    private List<WorkScheduleSummaryResponseDto> workSchedules;
}
