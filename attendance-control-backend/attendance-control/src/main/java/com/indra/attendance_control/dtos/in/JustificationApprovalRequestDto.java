package com.indra.attendance_control.dtos.in;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JustificationApprovalRequestDto {
    private Boolean approved;
}