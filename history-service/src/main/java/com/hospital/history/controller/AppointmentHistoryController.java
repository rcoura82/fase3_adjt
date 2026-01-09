package com.hospital.history.controller;

import com.hospital.history.entity.AppointmentHistory;
import com.hospital.history.repository.AppointmentHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class AppointmentHistoryController {
    
    private final AppointmentHistoryRepository repository;
    
    @QueryMapping
    public List<AppointmentHistory> patientAppointments(@Argument Long patientId) {
        return repository.findByPatientId(patientId);
    }
    
    @QueryMapping
    public List<AppointmentHistory> futureAppointments(@Argument Long patientId) {
        return repository.findByPatientIdAndAppointmentDateGreaterThan(patientId, LocalDateTime.now());
    }
    
    @QueryMapping
    public AppointmentHistory appointment(@Argument Long id) {
        return repository.findById(id).orElse(null);
    }
    
    @QueryMapping
    public List<AppointmentHistory> allAppointments() {
        return repository.findAll();
    }
}
