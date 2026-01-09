package com.hospital.scheduling.controller;

import com.hospital.scheduling.dto.AppointmentDTO;
import com.hospital.scheduling.dto.CreateAppointmentRequest;
import com.hospital.scheduling.dto.UpdateAppointmentRequest;
import com.hospital.scheduling.service.AppointmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/appointments")
@RequiredArgsConstructor
public class AppointmentController {
    
    private final AppointmentService appointmentService;
    
    @PostMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public ResponseEntity<AppointmentDTO> createAppointment(
            @Valid @RequestBody CreateAppointmentRequest request) {
        AppointmentDTO created = appointmentService.createAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("@appointmentSecurity.canAccess(#id, authentication)")
    public ResponseEntity<AppointmentDTO> getAppointment(
            @PathVariable Long id,
            Authentication authentication) {
        AppointmentDTO appointment = appointmentService.getAppointment(id);
        return ResponseEntity.ok(appointment);
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public ResponseEntity<List<AppointmentDTO>> getAllAppointments() {
        List<AppointmentDTO> appointments = appointmentService.getAllAppointments();
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("@appointmentSecurity.canAccessPatientAppointments(#patientId, authentication)")
    public ResponseEntity<List<AppointmentDTO>> getPatientAppointments(
            @PathVariable Long patientId,
            Authentication authentication) {
        List<AppointmentDTO> appointments = appointmentService.getPatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }
    
    @GetMapping("/patient/{patientId}/future")
    @PreAuthorize("@appointmentSecurity.canAccessPatientAppointments(#patientId, authentication)")
    public ResponseEntity<List<AppointmentDTO>> getFuturePatientAppointments(
            @PathVariable Long patientId,
            Authentication authentication) {
        List<AppointmentDTO> appointments = appointmentService.getFuturePatientAppointments(patientId);
        return ResponseEntity.ok(appointments);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public ResponseEntity<AppointmentDTO> updateAppointment(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAppointmentRequest request) {
        AppointmentDTO updated = appointmentService.updateAppointment(id, request);
        return ResponseEntity.ok(updated);
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'NURSE')")
    public ResponseEntity<Void> deleteAppointment(@PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.noContent().build();
    }
}
