package com.indra.attendance_control.controllers;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.indra.attendance_control.dtos.in.JustificationApprovalRequestDto;
import com.indra.attendance_control.dtos.in.JustificationRequestDto;
import com.indra.attendance_control.dtos.out.JustificationResponseDto;
import com.indra.attendance_control.services.interfaces.IJustificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(value = "/api/justifications", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class JustificationController {
    
    private final IJustificationService justificationService;
    
    /**
     * Empleado envía su justificación usando el ID del attendance record
     * PUT /api/justifications/attendance/{attendanceRecordId}
     */
    @PutMapping("/attendance/{attendanceRecordId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLOYEE')")
    public ResponseEntity<JustificationResponseDto> submitJustification(
            @PathVariable Long attendanceRecordId,
            @Valid @RequestBody JustificationRequestDto request) {
        
        JustificationResponseDto response = 
            justificationService.submitJustificationByAttendanceRecord(attendanceRecordId, request);
        
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/approval")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<JustificationResponseDto> updateApprovalStatus(
            @PathVariable Long id,
            @RequestBody JustificationApprovalRequestDto request) {
        
        JustificationResponseDto response = justificationService.updateApprovalStatus(id, request);
        
        return ResponseEntity.ok(response);
    }
}