package com.hospital.history.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "appointment_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentHistory {
    
    @Id
    private Long id;
    
    @Column(nullable = false)
    private Long patientId;
    
    @Column(nullable = false)
    private String patientName;
    
    @Column(nullable = false)
    private String patientEmail;
    
    @Column(nullable = false)
    private Long doctorId;
    
    @Column(nullable = false)
    private String doctorName;
    
    @Column(nullable = false)
    private LocalDateTime appointmentDate;
    
    @Column(length = 500)
    private String notes;
    
    @Column(nullable = false)
    private String status;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
