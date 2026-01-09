package com.hospital.scheduling.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentEvent implements Serializable {
    private Long appointmentId;
    private Long patientId;
    private String patientName;
    private String patientEmail;
    private Long doctorId;
    private String doctorName;
    private LocalDateTime appointmentDate;
    private String eventType; // CREATED, UPDATED, CANCELLED
}
