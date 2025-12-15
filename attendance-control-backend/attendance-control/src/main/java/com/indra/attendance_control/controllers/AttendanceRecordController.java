package com.indra.attendance_control.controllers;

import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.indra.attendance_control.commons.PaginationModel;
import com.indra.attendance_control.dtos.in.AttendanceRecordRequestDto;
import com.indra.attendance_control.dtos.out.AttendanceRecordResponseDto;
import com.indra.attendance_control.dtos.out.AttendanceRecordWithJustificationResponseDto;
import com.indra.attendance_control.dtos.out.TodayAttendanceDto;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.repositories.IEmployeeRepository;
import com.indra.attendance_control.services.impl.AttendanceRecordPaginationService;
import com.indra.attendance_control.services.interfaces.IAttendanceRecordService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "api/attendance", produces = MediaType.APPLICATION_JSON_VALUE)
public class AttendanceRecordController {
    private final IAttendanceRecordService attendanceRecordService;
    private final AttendanceRecordPaginationService attendanceRecordPaginationService;
    private final IEmployeeRepository employeeRepository;

    /**
     * Crear un nuevo registro de asistencia
     * POST /api/attendance
     */
    @PostMapping
    //@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ADMIN', 'SUPERVISOR')")
    public ResponseEntity<AttendanceRecordResponseDto> createAttendanceRecord(
            @Valid @RequestBody AttendanceRecordRequestDto request) {
        
        AttendanceRecordResponseDto response = attendanceRecordService.createAttendanceRecord(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/check-in/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AttendanceRecordResponseDto> checkInMe(Authentication authentication) {
        String username = authentication.getName();
        AttendanceRecordResponseDto response = attendanceRecordService.checkInByUsername(username);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/check-out/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<AttendanceRecordResponseDto> checkOutMe(Authentication authentication) {
        String username = authentication.getName();
        AttendanceRecordResponseDto response = attendanceRecordService.checkOutByUsername(username);
        return ResponseEntity.ok(response);
    }

    /**
     * Paginación de attendance records con justificaciones
     * POST /api/attendance/pagination
     */
    @PostMapping("/pagination")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PageImpl<AttendanceRecordWithJustificationResponseDto>> getPaginationRecordAttendance(
            @RequestBody PaginationModel paginationModel) {
        
        PageImpl<AttendanceRecordWithJustificationResponseDto> page = 
            attendanceRecordPaginationService.getPagination(paginationModel);
        
        return ResponseEntity.ok(page);
    }

    @PostMapping("/pagination/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<PageImpl<AttendanceRecordWithJustificationResponseDto>> getMyPagination(
            @RequestBody PaginationModel paginationModel,
            Authentication authentication) {
        
        String username = authentication.getName();
        
        // Obtener el employeeId del usuario autenticado
        Employee employee = employeeRepository.findByUserUsername(username)
            .orElseThrow(() -> new RuntimeException("No hay empleado asociado a este usuario"));
        
        // Agregar el employeeId al modelo de paginación automáticamente
        PageImpl<AttendanceRecordWithJustificationResponseDto> page = 
            attendanceRecordPaginationService.getPaginationByEmployee(paginationModel, employee.getIdEmployee());
        
        return ResponseEntity.ok(page);
    }

    @GetMapping("/today/me")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<TodayAttendanceDto> getMyTodayAttendance(Authentication authentication) {
        String username = authentication.getName();
        TodayAttendanceDto attendance = attendanceRecordService.getTodayAttendanceByUsername(username);
        return ResponseEntity.ok(attendance);
    }
}
