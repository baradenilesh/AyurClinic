package com.ayurclinic.patient.repository;

import com.ayurclinic.patient.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    Optional<Patient> findByPatientNumber(String patientNumber);

    boolean existsByMobile(String mobile);

    boolean existsByMobileAndIdNot(String mobile, UUID id);

    List<Patient> findByFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String firstName,
            String lastName
    );

    List<Patient> findByStatus(String status);
}