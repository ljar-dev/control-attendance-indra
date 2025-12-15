package com.indra.attendance_control.dtos.in;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class FirstLoginPasswordRequestDto {
    
    @NotBlank(message = "New password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String newPassword;

    // Getters y setters
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}