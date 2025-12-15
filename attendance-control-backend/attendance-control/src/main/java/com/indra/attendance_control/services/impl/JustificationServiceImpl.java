package com.indra.attendance_control.services.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.indra.attendance_control.dtos.in.JustificationApprovalRequestDto;
import com.indra.attendance_control.dtos.in.JustificationRequestDto;
import com.indra.attendance_control.dtos.out.JustificationResponseDto;
import com.indra.attendance_control.exceptions.ResourceNotFoundException;
import com.indra.attendance_control.exceptions.ValidatedRequestException;
import com.indra.attendance_control.models.Employee;
import com.indra.attendance_control.models.Justification;
import com.indra.attendance_control.repositories.IAttendanceRecordRepository;
import com.indra.attendance_control.repositories.IJustificationRepository;
import com.indra.attendance_control.services.interfaces.IJustificationService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class JustificationServiceImpl implements IJustificationService {
    
    private final IJustificationRepository justificationRepository;
    private final IAttendanceRecordRepository attendanceRecordRepository;
    
    private static final DateTimeFormatter DATETIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @Transactional
    public JustificationResponseDto submitJustificationByAttendanceRecord(
            Long attendanceRecordId, 
            JustificationRequestDto request) {
        
        // Buscar el attendance record
        attendanceRecordRepository.findById(attendanceRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Attendance record not found with id: " + attendanceRecordId));
        
        // Buscar la justificación asociada
        Justification justification = justificationRepository
                .findByAttendanceRecordIdAttendanceRecord(attendanceRecordId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No justification found for attendance record id: " + attendanceRecordId));
        
        // Validar que no haya sido enviada antes
        if (justification.getSubmittedAt() != null) {
            throw new ValidatedRequestException(
                "Justification already submitted at: " + 
                justification.getSubmittedAt().format(DATETIME_FORMATTER));
        }
        
        // Actualizar justificación
        justification.setJustificationText(request.getJustificationText());
        justification.setSubmittedAt(LocalDateTime.now());
        justification.setUpdatedAt(LocalDateTime.now());
        
        justification = justificationRepository.save(justification);
        
        return buildResponse(justification);
    }

    @Override
    @Transactional
    public JustificationResponseDto updateApprovalStatus(Long justificationId, JustificationApprovalRequestDto request) {
        
        Justification justification = justificationRepository.findById(justificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Justification not found with id: " + justificationId));
        
        if (justification.getJustificationText() == null || justification.getJustificationText().trim().isEmpty()) {
            throw new ValidatedRequestException("Cannot approve/reject a justification without text");
        }
        
        justification.setApproved(request.getApproved());
        justification.setUpdatedAt(LocalDateTime.now());
        
        justificationRepository.save(justification);
        
        return buildResponse(justification);
    }
    
    /**
     * Construye el DTO de respuesta
     */
    private JustificationResponseDto buildResponse(Justification justification) {
        Employee employee = justification.getAttendanceRecord().getEmployee();
        
        return JustificationResponseDto.builder()
                .idJustification(justification.getIdJustification())
                .employeeId(employee.getIdEmployee())
                .employeeCode(employee.getEmployeeCode())
                .employeeName(employee.getFirstName() + " " + employee.getLastName())
                .department(employee.getDepartment())
                .attendanceRecordId(justification.getAttendanceRecord().getIdAttendanceRecord())
                .attendanceDate(justification.getAttendanceRecord().getCreatedAt() != null ?
                    justification.getAttendanceRecord().getCreatedAt().toLocalDate().toString() : null)
                .checkIn(justification.getAttendanceRecord().getCheckIn() != null ?
                    justification.getAttendanceRecord().getCheckIn().format(DATETIME_FORMATTER) : null)
                .checkOut(justification.getAttendanceRecord().getCheckOut() != null ?
                    justification.getAttendanceRecord().getCheckOut().format(DATETIME_FORMATTER) : null)
                .attendanceStatus(justification.getAttendanceRecord().getStatus())
                .justificationText(justification.getJustificationText())
                .submittedAt(justification.getSubmittedAt() != null ?
                    justification.getSubmittedAt().format(DATETIME_FORMATTER) : null)
                .createdAt(justification.getCreatedAt() != null ?
                    justification.getCreatedAt().format(DATETIME_FORMATTER) : null)
                .updatedAt(justification.getUpdatedAt() != null ?
                    justification.getUpdatedAt().format(DATETIME_FORMATTER) : null)
                .build();
    }
}