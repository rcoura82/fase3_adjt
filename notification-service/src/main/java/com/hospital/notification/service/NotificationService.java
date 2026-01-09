package com.hospital.notification.service;

import com.hospital.notification.event.AppointmentEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {
    
    @Value("${notification.enabled}")
    private boolean notificationEnabled;
    
    private static final DateTimeFormatter DATE_FORMATTER = 
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    public void sendAppointmentCreatedNotification(AppointmentEvent event) {
        if (!notificationEnabled) {
            log.info("Notifications disabled");
            return;
        }
        
        String message = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been scheduled!\n\n" +
                "Details:\n" +
                "- Doctor: %s\n" +
                "- Date/Time: %s\n" +
                "- Appointment ID: %d\n\n" +
                "Please arrive 15 minutes before your appointment time.\n\n" +
                "Thank you,\nHospital Management System",
                event.getPatientName(),
                event.getDoctorName(),
                event.getAppointmentDate().format(DATE_FORMATTER),
                event.getAppointmentId()
        );
        
        sendNotification(event.getPatientEmail(), "Appointment Scheduled", message);
    }
    
    public void sendAppointmentUpdatedNotification(AppointmentEvent event) {
        if (!notificationEnabled) {
            log.info("Notifications disabled");
            return;
        }
        
        String message = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been updated!\n\n" +
                "Details:\n" +
                "- Doctor: %s\n" +
                "- Date/Time: %s\n" +
                "- Appointment ID: %d\n\n" +
                "Please check the updated information carefully.\n\n" +
                "Thank you,\nHospital Management System",
                event.getPatientName(),
                event.getDoctorName(),
                event.getAppointmentDate().format(DATE_FORMATTER),
                event.getAppointmentId()
        );
        
        sendNotification(event.getPatientEmail(), "Appointment Updated", message);
    }
    
    public void sendAppointmentCancelledNotification(AppointmentEvent event) {
        if (!notificationEnabled) {
            log.info("Notifications disabled");
            return;
        }
        
        String message = String.format(
                "Dear %s,\n\n" +
                "Your appointment has been cancelled.\n\n" +
                "Cancelled Appointment Details:\n" +
                "- Doctor: %s\n" +
                "- Date/Time: %s\n" +
                "- Appointment ID: %d\n\n" +
                "If you did not request this cancellation, please contact us immediately.\n\n" +
                "Thank you,\nHospital Management System",
                event.getPatientName(),
                event.getDoctorName(),
                event.getAppointmentDate().format(DATE_FORMATTER),
                event.getAppointmentId()
        );
        
        sendNotification(event.getPatientEmail(), "Appointment Cancelled", message);
    }
    
    private void sendNotification(String email, String subject, String message) {
        // In a real system, this would send an actual email
        // For now, we'll just log it
        log.info("===== NOTIFICATION =====");
        log.info("To: {}", email);
        log.info("Subject: {}", subject);
        log.info("Message:\n{}", message);
        log.info("========================");
        
        // Uncomment below to send actual emails (requires proper SMTP configuration)
        /*
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(email);
            mailMessage.setSubject(subject);
            mailMessage.setText(message);
            mailMessage.setFrom(fromEmail);
            mailSender.send(mailMessage);
            log.info("Email sent successfully to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send email to: {}", email, e);
        }
        */
    }
}
