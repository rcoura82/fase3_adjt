package com.hospital.history.listener;

import com.hospital.history.entity.AppointmentHistory;
import com.hospital.history.event.AppointmentEvent;
import com.hospital.history.repository.AppointmentHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentHistoryListener {
    
    private final AppointmentHistoryRepository repository;
    
    @RabbitListener(queues = "${rabbitmq.queue.created}")
    public void handleAppointmentCreated(AppointmentEvent event) {
        log.info("Syncing appointment created to history: {}", event.getAppointmentId());
        
        try {
            AppointmentHistory history = new AppointmentHistory();
            history.setId(event.getAppointmentId());
            history.setPatientId(event.getPatientId());
            history.setPatientName(event.getPatientName());
            history.setPatientEmail(event.getPatientEmail());
            history.setDoctorId(event.getDoctorId());
            history.setDoctorName(event.getDoctorName());
            history.setAppointmentDate(event.getAppointmentDate());
            history.setStatus("SCHEDULED");
            history.setCreatedAt(LocalDateTime.now());
            history.setUpdatedAt(LocalDateTime.now());
            
            repository.save(history);
            log.info("Successfully synced appointment to history: {}", event.getAppointmentId());
        } catch (Exception e) {
            log.error("Error syncing appointment to history: {}", event.getAppointmentId(), e);
        }
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.updated}")
    public void handleAppointmentUpdated(AppointmentEvent event) {
        log.info("Syncing appointment update to history: {}", event.getAppointmentId());
        
        try {
            repository.findById(event.getAppointmentId()).ifPresent(history -> {
                history.setAppointmentDate(event.getAppointmentDate());
                history.setUpdatedAt(LocalDateTime.now());
                
                if ("CANCELLED".equals(event.getEventType())) {
                    history.setStatus("CANCELLED");
                }
                
                repository.save(history);
                log.info("Successfully updated appointment in history: {}", event.getAppointmentId());
            });
        } catch (Exception e) {
            log.error("Error updating appointment in history: {}", event.getAppointmentId(), e);
        }
    }
}
