package com.indra.attendance_control.services.interfaces;

import com.indra.attendance_control.dtos.in.AttendanceRecordRequestDto;
import com.indra.attendance_control.dtos.out.AttendanceRecordResponseDto;
import com.indra.attendance_control.dtos.out.TodayAttendanceDto;

public interface IAttendanceRecordService {
    AttendanceRecordResponseDto createAttendanceRecord(AttendanceRecordRequestDto request);

    AttendanceRecordResponseDto checkInByUsername(String username);

    AttendanceRecordResponseDto checkOutByUsername(String username);

    TodayAttendanceDto getTodayAttendanceByUsername(String username);
}
