package com.indra.attendance_control.dtos.in;

import java.time.LocalDateTime;
import com.indra.attendance_control.models.enums.AttendanceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AttendanceRecordRequestDto {
    private Long idEmployee;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private AttendanceStatus status;
}
