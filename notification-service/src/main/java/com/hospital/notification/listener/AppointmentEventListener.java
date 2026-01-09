package com.hospital.notification.listener;

import com.hospital.notification.event.AppointmentEvent;
import com.hospital.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventListener {
    
    private final NotificationService notificationService;
    
    @RabbitListener(queues = "${rabbitmq.queue.created}")
    public void handleAppointmentCreated(AppointmentEvent event) {
        log.info("Received appointment created event: {}", event.getAppointmentId());
        
        try {
            notificationService.sendAppointmentCreatedNotification(event);
            log.info("Successfully processed appointment created event: {}", event.getAppointmentId());
        } catch (Exception e) {
            log.error("Error processing appointment created event: {}", event.getAppointmentId(), e);
            // In a real system, you might want to send this to a dead-letter queue
        }
    }
    
    @RabbitListener(queues = "${rabbitmq.queue.updated}")
    public void handleAppointmentUpdated(AppointmentEvent event) {
        log.info("Received appointment updated event: {}", event.getAppointmentId());
        
        try {
            if ("CANCELLED".equals(event.getEventType())) {
                notificationService.sendAppointmentCancelledNotification(event);
            } else {
                notificationService.sendAppointmentUpdatedNotification(event);
            }
            log.info("Successfully processed appointment updated event: {}", event.getAppointmentId());
        } catch (Exception e) {
            log.error("Error processing appointment updated event: {}", event.getAppointmentId(), e);
            // In a real system, you might want to send this to a dead-letter queue
        }
    }
}
