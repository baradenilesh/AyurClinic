package com.ayurclinic.doctor.service;

import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.doctor.dto.CreateDoctorRequest;
import com.ayurclinic.doctor.dto.DoctorResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorStatusRequest;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private DoctorService doctorService;

    @Test
    void createDoctor_whenValidRequest_createsDoctorForTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CreateDoctorRequest request = new CreateDoctorRequest();

        request.setClinicId(clinicId);
        request.setFirstName("Dr. Rahul");
        request.setLastName("Sharma");
        request.setQualification("BAMS");
        request.setSpecialization("Panchakarma");
        request.setRegistrationNumber("AYU12345");
        request.setExperienceYears(10);
        request.setBio("Ayurvedic physician");
        request.setConsultationFee(
                new BigDecimal("500.00")
        );

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(new Clinic()));

        when(doctorRepository
                .existsByRegistrationNumberAndTenantId(
                        "AYU12345",
                        tenantId
                ))
                .thenReturn(false);

        Doctor savedDoctor = new Doctor();

        savedDoctor.setId(UUID.randomUUID());
        savedDoctor.setTenantId(tenantId);
        savedDoctor.setClinicId(clinicId);
        savedDoctor.setFirstName("Dr. Rahul");
        savedDoctor.setLastName("Sharma");
        savedDoctor.setQualification("BAMS");
        savedDoctor.setSpecialization("Panchakarma");
        savedDoctor.setRegistrationNumber("AYU12345");
        savedDoctor.setExperienceYears(10);
        savedDoctor.setStatus("ACTIVE");

        when(doctorRepository.save(any(Doctor.class)))
                .thenReturn(savedDoctor);

        DoctorResponse response =
                doctorService.createDoctor(
                        tenantId,
                        request
                );

        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals(clinicId, response.getClinicId());
        assertEquals("Dr. Rahul", response.getFirstName());
        assertEquals("BAMS", response.getQualification());
        assertEquals("AYU12345",
                response.getRegistrationNumber());
        assertEquals("ACTIVE", response.getStatus());

        verify(doctorRepository)
                .save(any(Doctor.class));
    }

    @Test
    void createDoctor_whenClinicBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CreateDoctorRequest request =
                new CreateDoctorRequest();

        request.setClinicId(clinicId);
        request.setFirstName("Dr. Rahul");

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorService.createDoctor(
                        tenantId,
                        request
                )
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void createDoctor_whenRegistrationNumberExists_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CreateDoctorRequest request =
                new CreateDoctorRequest();

        request.setClinicId(clinicId);
        request.setFirstName("Dr. Rahul");
        request.setRegistrationNumber("AYU12345");

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(new Clinic()));

        when(doctorRepository
                .existsByRegistrationNumberAndTenantId(
                        "AYU12345",
                        tenantId
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorService.createDoctor(
                                tenantId,
                                request
                        )
                );

        assertEquals(
                "Doctor with registration number 'AYU12345' already exists",
                exception.getMessage()
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void getDoctor_whenDoctorBelongsToTenant_returnsDoctor() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Doctor doctor = new Doctor();

        doctor.setId(doctorId);
        doctor.setTenantId(tenantId);
        doctor.setClinicId(clinicId);
        doctor.setFirstName("Dr. Rahul");
        doctor.setQualification("BAMS");
        doctor.setStatus("ACTIVE");

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(doctor));

        DoctorResponse response =
                doctorService.getDoctor(
                        tenantId,
                        doctorId
                );

        assertEquals(doctorId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals("Dr. Rahul", response.getFirstName());
    }

    @Test
    void getDoctor_whenDoctorBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorService.getDoctor(
                        tenantId,
                        doctorId
                )
        );
    }

    @Test
    void getDoctors_shouldReturnDoctorsForTenant() {

        UUID tenantId = UUID.randomUUID();

        Doctor doctor = new Doctor();

        doctor.setId(UUID.randomUUID());
        doctor.setTenantId(tenantId);
        doctor.setClinicId(UUID.randomUUID());
        doctor.setFirstName("Dr. Rahul");
        doctor.setStatus("ACTIVE");

        when(doctorRepository.findByTenantId(tenantId))
                .thenReturn(List.of(doctor));

        List<DoctorResponse> response =
                doctorService.getDoctors(tenantId);

        assertEquals(1, response.size());
        assertEquals(
                "Dr. Rahul",
                response.get(0).getFirstName()
        );

        verify(doctorRepository)
                .findByTenantId(tenantId);
    }

    @Test
    void getDoctorsByClinic_shouldReturnDoctorsForClinic() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Doctor doctor = new Doctor();

        doctor.setId(UUID.randomUUID());
        doctor.setTenantId(tenantId);
        doctor.setClinicId(clinicId);
        doctor.setFirstName("Dr. Rahul");
        doctor.setStatus("ACTIVE");

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(new Clinic()));

        when(doctorRepository.findByTenantIdAndClinicId(
                tenantId,
                clinicId
        )).thenReturn(List.of(doctor));

        List<DoctorResponse> response =
                doctorService.getDoctorsByClinic(
                        tenantId,
                        clinicId
                );

        assertEquals(1, response.size());
        assertEquals(
                clinicId,
                response.get(0).getClinicId()
        );
    }

    @Test
    void updateDoctor_whenValidRequest_updatesDoctor() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Doctor doctor = new Doctor();

        doctor.setId(doctorId);
        doctor.setTenantId(tenantId);
        doctor.setClinicId(clinicId);
        doctor.setFirstName("Dr. Rahul");
        doctor.setRegistrationNumber("AYU12345");
        doctor.setStatus("ACTIVE");

        UpdateDoctorRequest request =
                new UpdateDoctorRequest();

        request.setClinicId(clinicId);
        request.setFirstName("Dr. Rahul Updated");
        request.setQualification("BAMS MD");
        request.setSpecialization("Ayurveda");
        request.setRegistrationNumber("AYU12345");
        request.setExperienceYears(12);
        request.setConsultationFee(
                new BigDecimal("750.00")
        );

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(doctor));

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        DoctorResponse response =
                doctorService.updateDoctor(
                        tenantId,
                        doctorId,
                        request
                );

        assertEquals(
                "Dr. Rahul Updated",
                response.getFirstName()
        );

        assertEquals(
                "BAMS MD",
                response.getQualification()
        );

        assertEquals(
                "Ayurveda",
                response.getSpecialization()
        );

        assertEquals(
                12,
                response.getExperienceYears()
        );

        verify(doctorRepository)
                .save(doctor);
    }

    @Test
    void updateDoctor_whenDoctorBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        UpdateDoctorRequest request =
                new UpdateDoctorRequest();

        request.setFirstName("Updated Doctor");

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorService.updateDoctor(
                        tenantId,
                        doctorId,
                        request
                )
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void updateDoctor_whenRegistrationNumberBelongsToAnotherDoctor_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Doctor doctor = new Doctor();

        doctor.setId(doctorId);
        doctor.setTenantId(tenantId);
        doctor.setClinicId(clinicId);
        doctor.setFirstName("Dr. Rahul");
        doctor.setRegistrationNumber("AYU12345");

        UpdateDoctorRequest request =
                new UpdateDoctorRequest();

        request.setFirstName("Dr. Rahul Updated");
        request.setRegistrationNumber("AYU99999");

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(doctor));

        when(doctorRepository
                .existsByRegistrationNumberAndTenantIdAndIdNot(
                        "AYU99999",
                        tenantId,
                        doctorId
                ))
                .thenReturn(true);

        assertThrows(
                IllegalArgumentException.class,
                () -> doctorService.updateDoctor(
                        tenantId,
                        doctorId,
                        request
                )
        );

        verify(doctorRepository, never())
                .save(any(Doctor.class));
    }

    @Test
    void updateDoctorStatus_whenValidStatus_updatesStatus() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        Doctor doctor = new Doctor();

        doctor.setId(doctorId);
        doctor.setTenantId(tenantId);
        doctor.setFirstName("Dr. Rahul");
        doctor.setStatus("ACTIVE");

        UpdateDoctorStatusRequest request =
                new UpdateDoctorStatusRequest();

        request.setStatus("INACTIVE");

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(doctor));

        when(doctorRepository.save(doctor))
                .thenReturn(doctor);

        DoctorResponse response =
                doctorService.updateDoctorStatus(
                        tenantId,
                        doctorId,
                        request
                );

        assertEquals(
                "INACTIVE",
                response.getStatus()
        );
    }

    @Test
    void updateDoctorStatus_whenInvalidStatus_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        UpdateDoctorStatusRequest request =
                new UpdateDoctorStatusRequest();

        request.setStatus("DELETED");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorService.updateDoctorStatus(
                                tenantId,
                                doctorId,
                                request
                        )
                );

        assertEquals(
                "Status must be ACTIVE or INACTIVE",
                exception.getMessage()
        );

        verifyNoInteractions(doctorRepository);
    }

    @Test
    void createDoctor_whenTenantIdIsNull_throwsBadRequest() {

        CreateDoctorRequest request =
                new CreateDoctorRequest();

        request.setClinicId(UUID.randomUUID());
        request.setFirstName("Dr. Rahul");

        assertThrows(
                IllegalArgumentException.class,
                () -> doctorService.createDoctor(
                        null,
                        request
                )
        );

        verifyNoInteractions(
                doctorRepository,
                clinicRepository
        );
    }
}