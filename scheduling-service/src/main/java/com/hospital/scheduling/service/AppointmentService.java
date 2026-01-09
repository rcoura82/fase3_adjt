package com.hospital.scheduling.service;

import com.hospital.scheduling.dto.AppointmentDTO;
import com.hospital.scheduling.dto.CreateAppointmentRequest;
import com.hospital.scheduling.dto.UpdateAppointmentRequest;
import com.hospital.scheduling.entity.Appointment;
import com.hospital.scheduling.event.AppointmentEvent;
import com.hospital.scheduling.exception.AppointmentNotFoundException;
import com.hospital.scheduling.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final RabbitTemplate rabbitTemplate;
    
    @Value("${rabbitmq.exchange.name}")
    private String exchangeName;
    
    @Value("${rabbitmq.routing-key.created}")
    private String createdRoutingKey;
    
    @Value("${rabbitmq.routing-key.updated}")
    private String updatedRoutingKey;
    
    @Transactional
    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        log.info("Creating appointment for patient: {}", request.getPatientName());
        
        Appointment appointment = new Appointment();
        appointment.setPatientId(request.getPatientId());
        appointment.setPatientName(request.getPatientName());
        appointment.setPatientEmail(request.getPatientEmail());
        appointment.setDoctorId(request.getDoctorId());
        appointment.setDoctorName(request.getDoctorName());
        appointment.setAppointmentDate(request.getAppointmentDate());
        appointment.setNotes(request.getNotes());
        
        Appointment saved = appointmentRepository.save(appointment);
        
        // Publish event to RabbitMQ
        publishAppointmentEvent(saved, "CREATED", createdRoutingKey);
        
        return mapToDTO(saved);
    }
    
    @Transactional
    public AppointmentDTO updateAppointment(Long id, UpdateAppointmentRequest request) {
        log.info("Updating appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
        
        if (request.getAppointmentDate() != null) {
            appointment.setAppointmentDate(request.getAppointmentDate());
        }
        if (request.getNotes() != null) {
            appointment.setNotes(request.getNotes());
        }
        if (request.getStatus() != null) {
            appointment.setStatus(request.getStatus());
        }
        
        Appointment updated = appointmentRepository.save(appointment);
        
        // Publish event to RabbitMQ
        publishAppointmentEvent(updated, "UPDATED", updatedRoutingKey);
        
        return mapToDTO(updated);
    }
    
    @Transactional(readOnly = true)
    public AppointmentDTO getAppointment(Long id) {
        log.info("Fetching appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
        
        return mapToDTO(appointment);
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAllAppointments() {
        log.info("Fetching all appointments");
        
        return appointmentRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointments(Long patientId) {
        log.info("Fetching appointments for patient: {}", patientId);
        
        return appointmentRepository.findByPatientId(patientId).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<AppointmentDTO> getFuturePatientAppointments(Long patientId) {
        log.info("Fetching future appointments for patient: {}", patientId);
        
        return appointmentRepository
                .findByPatientIdAndAppointmentDateGreaterThan(patientId, LocalDateTime.now())
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void deleteAppointment(Long id) {
        log.info("Deleting appointment: {}", id);
        
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new AppointmentNotFoundException("Appointment not found with id: " + id));
        
        appointmentRepository.delete(appointment);
        
        // Publish cancellation event
        publishAppointmentEvent(appointment, "CANCELLED", updatedRoutingKey);
    }
    
    private void publishAppointmentEvent(Appointment appointment, String eventType, String routingKey) {
        AppointmentEvent event = new AppointmentEvent(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getPatientName(),
                appointment.getPatientEmail(),
                appointment.getDoctorId(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate(),
                eventType
        );
        
        log.info("Publishing {} event for appointment: {}", eventType, appointment.getId());
        rabbitTemplate.convertAndSend(exchangeName, routingKey, event);
    }
    
    private AppointmentDTO mapToDTO(Appointment appointment) {
        return new AppointmentDTO(
                appointment.getId(),
                appointment.getPatientId(),
                appointment.getPatientName(),
                appointment.getPatientEmail(),
                appointment.getDoctorId(),
                appointment.getDoctorName(),
                appointment.getAppointmentDate(),
                appointment.getNotes(),
                appointment.getStatus(),
                appointment.getCreatedAt(),
                appointment.getUpdatedAt()
        );
    }
}
