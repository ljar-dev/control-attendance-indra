package com.indra.attendance_control.dtos.in;

import com.indra.attendance_control.models.enums.JustificationStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewJustificationRequestDto {
    
    @NotNull(message = "Status is required")
    private JustificationStatus status; // APPROVED o REJECTED
    
    @Size(max = 500, message = "Comments must not exceed 500 characters")
    private String adminComments;
}