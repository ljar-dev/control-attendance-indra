package com.indra.attendance_control.models;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "justifications")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Justification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_justification")
    private Long idJustification;
    
    @OneToOne
    @JoinColumn(name = "attendance_record_id", nullable = false, unique = true)
    private AttendanceRecord attendanceRecord;
    
    @Column(name = "justification_text", length = 1000)
    private String justificationText; // NULL = pendiente, NOT NULL = completado
    
    @Column(name = "submitted_at")
    private LocalDateTime submittedAt; // Fecha cuando el empleado envió la justificación
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "approved")
    private Boolean approved;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}