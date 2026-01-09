package com.hospital.scheduling.security;

import com.hospital.scheduling.entity.Appointment;
import com.hospital.scheduling.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component("appointmentSecurity")
@RequiredArgsConstructor
public class AppointmentSecurityService {
    
    private final AppointmentRepository appointmentRepository;
    
    public boolean canAccess(Long appointmentId, Authentication authentication) {
        // Doctors and nurses can access all appointments
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCTOR")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_NURSE"))) {
            return true;
        }
        
        // Patients can only access their own appointments
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_PATIENT"))) {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment != null) {
                // In a real system, we would get the patient ID from the authenticated user
                // For simplicity, we're checking if the username matches the patient name
                return appointment.getPatientName().equalsIgnoreCase(authentication.getName());
            }
        }
        
        return false;
    }
    
    public boolean canAccessPatientAppointments(Long patientId, Authentication authentication) {
        // Doctors and nurses can access all patient appointments
        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_DOCTOR")) ||
            authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_NURSE"))) {
            return true;
        }
        
        // Patients can only access their own appointments
        // In a real system, we would compare the patientId with the authenticated user's ID
        return false;
    }
}
