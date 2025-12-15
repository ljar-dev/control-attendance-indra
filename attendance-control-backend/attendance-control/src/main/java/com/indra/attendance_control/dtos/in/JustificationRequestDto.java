package com.indra.attendance_control.dtos.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JustificationRequestDto {
    
    @NotBlank(message = "Justification text is required")
    @Size(min = 10, max = 1000, message = "Justification must be between 10 and 1000 characters")
    private String justificationText;
}