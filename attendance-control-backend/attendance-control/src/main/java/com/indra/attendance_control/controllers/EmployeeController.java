package com.indra.attendance_control.controllers;

import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.indra.attendance_control.commons.PaginationModel;
import com.indra.attendance_control.dtos.in.EmployeeRequestDto;
import com.indra.attendance_control.dtos.out.EmployeeListResponseDto;
import com.indra.attendance_control.dtos.out.EmployeeResponseDto;
import com.indra.attendance_control.services.impl.EmployeePaginationService;
import com.indra.attendance_control.services.interfaces.IEmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/employee", produces = MediaType.APPLICATION_JSON_VALUE)
public class EmployeeController {
    private final IEmployeeService employeeService;
    private final EmployeePaginationService employeePaginationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> createEmployee(
            @Valid @RequestBody EmployeeRequestDto request) {
        
        EmployeeResponseDto response = employeeService.createEmployee(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Paginación de empleados
     * POST /api/employee/pagination
     */
    @PostMapping("/pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageImpl<EmployeeListResponseDto>> getPaginationEmployees(
            @RequestBody PaginationModel paginationModel) {
        
        PageImpl<EmployeeListResponseDto> page = 
            employeePaginationService.getPagination(paginationModel);
        
        return ResponseEntity.ok(page);
    }

    /**
     * Obtener un empleado por ID
     * GET /api/employee/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id) {
        EmployeeResponseDto response = employeeService.getEmployeeById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Actualizar un empleado
     * PUT /api/employee/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeRequestDto request) {
        
        EmployeeResponseDto response = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Desactivar/Activar un empleado (soft delete)
     * PATCH /api/employee/{id}/toggle-status
     */
    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmployeeResponseDto> toggleEmployeeStatus(@PathVariable Long id) {
        EmployeeResponseDto response = employeeService.toggleEmployeeStatus(id);
        return ResponseEntity.ok(response);
    }
}
