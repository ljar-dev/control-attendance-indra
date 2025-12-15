package com.indra.attendance_control.dtos.out;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TodayAttendanceDto {
    private Long employeeId;
    private String employeeName;
    private LocalDateTime checkIn;
    private LocalDateTime checkOut;
    private boolean hasCheckIn;
    private boolean hasCheckOut;
    private String status;
}
