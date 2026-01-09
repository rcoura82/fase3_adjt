package com.hospital.scheduling.dto;

import com.hospital.scheduling.entity.AppointmentStatus;
import jakarta.validation.constraints.Future;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateAppointmentRequest {
    
    @Future(message = "Appointment date must be in the future")
    private LocalDateTime appointmentDate;
    
    private String notes;
    
    private AppointmentStatus status;
}
