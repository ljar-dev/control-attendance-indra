package com.indra.attendance_control.dtos.in;

import java.time.DayOfWeek;
import java.time.LocalTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.indra.attendance_control.commons.FlexibleLocalTimeDeserializer;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WorkScheduleRequestDto {
    
    @NotNull(message = "Day of week is required")
    private DayOfWeek dayOfWeek;
    
    @NotNull(message = "Start time is required")
    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    private LocalTime startTime;
    
    @NotNull(message = "End time is required")
    @JsonDeserialize(using = FlexibleLocalTimeDeserializer.class)
    private LocalTime endTime;
    
    @Builder.Default
    private boolean enabled = true;
}