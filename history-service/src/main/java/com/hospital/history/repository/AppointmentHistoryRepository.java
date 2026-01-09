package com.hospital.history.repository;

import com.hospital.history.entity.AppointmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentHistoryRepository extends JpaRepository<AppointmentHistory, Long> {
    
    List<AppointmentHistory> findByPatientId(Long patientId);
    
    List<AppointmentHistory> findByPatientIdAndAppointmentDateGreaterThan(Long patientId, LocalDateTime date);
    
    List<AppointmentHistory> findByDoctorId(Long doctorId);
}
