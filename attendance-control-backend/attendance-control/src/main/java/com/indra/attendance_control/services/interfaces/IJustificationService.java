package com.indra.attendance_control.services.interfaces;

import com.indra.attendance_control.dtos.in.JustificationApprovalRequestDto;
import com.indra.attendance_control.dtos.in.JustificationRequestDto;
import com.indra.attendance_control.dtos.out.JustificationResponseDto;

public interface IJustificationService {
    
    JustificationResponseDto submitJustificationByAttendanceRecord(
        Long attendanceRecordId, 
        JustificationRequestDto request
    );

    JustificationResponseDto updateApprovalStatus(Long justificationId, JustificationApprovalRequestDto request);
}