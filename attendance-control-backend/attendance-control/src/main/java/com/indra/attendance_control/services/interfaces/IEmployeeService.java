package com.indra.attendance_control.services.interfaces;

import com.indra.attendance_control.dtos.in.EmployeeRequestDto;
import com.indra.attendance_control.dtos.out.EmployeeResponseDto;

public interface IEmployeeService {
    EmployeeResponseDto createEmployee(EmployeeRequestDto request);

    EmployeeResponseDto getEmployeeById(Long id);
    
    EmployeeResponseDto updateEmployee(Long id, EmployeeRequestDto request);
    
    EmployeeResponseDto toggleEmployeeStatus(Long id);
}
