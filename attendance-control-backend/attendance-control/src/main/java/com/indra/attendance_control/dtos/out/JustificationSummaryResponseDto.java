package com.indra.attendance_control.dtos.out;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JustificationSummaryResponseDto {
    private Long idJustification;
    private String justificationText;
    private String submittedAt;
    private Boolean approved;
}
