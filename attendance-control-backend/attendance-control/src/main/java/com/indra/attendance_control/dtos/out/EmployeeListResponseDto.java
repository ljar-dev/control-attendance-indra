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
public class EmployeeListResponseDto {
    private Long idEmployee;
    private String employeeCode;
    private String firstName;
    private String lastName;
    private String fullName;
    private String department;
    private String position;
    private LocalDate hireDate;
    private boolean enabled;
    private Long userId;
    private String username;
    private boolean userEnabled;
    private boolean mustChangePassword;
    private List<WorkScheduleSummaryResponseDto> workSchedules;
    private String createdAt;
    private String updatedAt;
}