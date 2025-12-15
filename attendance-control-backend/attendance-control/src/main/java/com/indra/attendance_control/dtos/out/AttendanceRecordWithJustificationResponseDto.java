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
public class AttendanceRecordWithJustificationResponseDto {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String department;
    private Long idAttendanceRecord;
    private String attendanceDate;
    private String checkIn;
    private String checkOut;
    private AttendanceStatus status;
    private JustificationSummaryResponseDto justification;
    private String createdAt;
    private String updatedAt;
}
