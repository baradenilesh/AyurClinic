package com.ayurclinic.appointment.service;

import com.ayurclinic.appointment.dto.AppointmentCreateRequest;
import com.ayurclinic.appointment.dto.AppointmentRescheduleRequest;
import com.ayurclinic.appointment.dto.AppointmentResponse;
import com.ayurclinic.appointment.dto.AppointmentSearchRequest;
import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.enums.AppointmentStatus;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.entity.DoctorAvailability;
import com.ayurclinic.doctor.repository.DoctorAvailabilityRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @InjectMocks
    private AppointmentService appointmentService;

    private UUID tenantId;
    private UUID clinicId;
    private UUID doctorId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        patientId = UUID.randomUUID();
    }

    @Test
    void shouldRejectNullTenantId() {

        AppointmentCreateRequest request =
                createValidRequest();

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(
                        null,
                        request
                )
        );

        verifyNoInteractions(
                appointmentRepository,
                clinicRepository,
                doctorRepository,
                patientRepository,
                doctorAvailabilityRepository
        );
    }

    @Test
    void shouldRejectNullAppointmentRequest() {

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        null
                )
        );

        verifyNoInteractions(
                appointmentRepository,
                clinicRepository,
                doctorRepository,
                patientRepository,
                doctorAvailabilityRepository
        );
    }

    @Test
    void shouldRejectPastAppointmentDate() {

        AppointmentCreateRequest request =
                createValidRequest();

        request.setAppointmentDate(
                LocalDate.now().minusDays(1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        request
                )
        );
    }

    @Test
    void shouldRejectInvalidAppointmentTime() {

        AppointmentCreateRequest request =
                createValidRequest();

        request.setStartTime(
                LocalTime.of(11, 0)
        );

        request.setEndTime(
                LocalTime.of(10, 30)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        request
                )
        );
    }

    @Test
    void shouldRejectClinicFromAnotherTenant() {

        AppointmentCreateRequest request =
                createValidRequest();

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        request
                )
        );

        verify(clinicRepository)
                .findByIdAndTenantId(
                        clinicId,
                        tenantId
                );
    }

    @Test
    void shouldRejectDoctorFromAnotherTenant() {

        AppointmentCreateRequest request =
                createValidRequest();

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(mockClinic()));

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        request
                )
        );
    }

    @Test
    void shouldRejectPatientFromAnotherTenant() {

        AppointmentCreateRequest request =
                createValidRequest();

        Doctor doctor = mockDoctor();

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(mockClinic()));

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(doctor));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                jakarta.persistence.EntityNotFoundException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        request
                )
        );
    }

    @Test
    void shouldRejectDoubleBooking() {

        AppointmentCreateRequest request =
                createValidRequest();

        Doctor doctor = mockDoctor();
        Patient patient = mockPatient();
        Clinic clinic = mockClinic();
        DoctorAvailability availability = mockAvailability();

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(Optional.of(clinic));

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(doctor));

        when(patientRepository.findByIdAndTenantId(
                patientId,
                tenantId
        )).thenReturn(Optional.of(patient));

        when(doctorAvailabilityRepository
                .findByTenantIdAndDoctorIdAndStatusOrderByDayOfWeek(
                        eq(tenantId),
                        eq(doctorId),
                        eq("ACTIVE")
                ))
                .thenReturn(List.of(availability));

        when(appointmentRepository
                .findByTenantIdAndDoctorIdAndAppointmentDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(tenantId),
                        eq(doctorId),
                        eq(request.getAppointmentDate()),
                        anyList(),
                        eq(request.getEndTime()),
                        eq(request.getStartTime())
                ))
                .thenReturn(List.of(mock(Appointment.class)));

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.createAppointment(
                        tenantId,
                        request
                )
        );

        verify(appointmentRepository)
                .findByTenantIdAndDoctorIdAndAppointmentDateAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        eq(tenantId),
                        eq(doctorId),
                        eq(request.getAppointmentDate()),
                        anyList(),
                        eq(request.getEndTime()),
                        eq(request.getStartTime())
                );
    }

    @Test
    void shouldConfirmRequestedAppointment() {

        UUID appointmentId = UUID.randomUUID();

        Appointment appointment =
                new Appointment();

        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setStatus(
                AppointmentStatus.REQUESTED
        );

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(
                appointment
        )).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.confirmAppointment(
                        tenantId,
                        appointmentId
                );

        assertEquals(
                AppointmentStatus.CONFIRMED,
                response.getStatus()
        );

        verify(appointmentRepository)
                .save(appointment);
    }

    @Test
    void shouldRejectConfirmingNonRequestedAppointment() {

        UUID appointmentId = UUID.randomUUID();

        Appointment appointment =
                new Appointment();

        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        assertThrows(
                IllegalArgumentException.class,
                () -> appointmentService.confirmAppointment(
                        tenantId,
                        appointmentId
                )
        );

        verify(appointmentRepository, never())
                .save(any());
    }

    @Test
    void shouldCancelConfirmedAppointment() {

        UUID appointmentId = UUID.randomUUID();

        Appointment appointment =
                new Appointment();

        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(
                appointment
        )).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.cancelAppointment(
                        tenantId,
                        appointmentId
                );

        assertEquals(
                AppointmentStatus.CANCELLED,
                response.getStatus()
        );
    }

    @Test
    void shouldCompleteConfirmedAppointment() {

        UUID appointmentId = UUID.randomUUID();

        Appointment appointment =
                new Appointment();

        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(
                appointment
        )).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.completeAppointment(
                        tenantId,
                        appointmentId
                );

        assertEquals(
                AppointmentStatus.COMPLETED,
                response.getStatus()
        );
    }

    @Test
    void shouldMarkConfirmedAppointmentAsNoShow() {

        UUID appointmentId = UUID.randomUUID();

        Appointment appointment =
                new Appointment();

        appointment.setId(appointmentId);
        appointment.setTenantId(tenantId);
        appointment.setStatus(
                AppointmentStatus.CONFIRMED
        );

        when(appointmentRepository.findById(
                appointmentId
        )).thenReturn(Optional.of(appointment));

        when(appointmentRepository.save(
                appointment
        )).thenReturn(appointment);

        AppointmentResponse response =
                appointmentService.markNoShow(
                        tenantId,
                        appointmentId
                );

        assertEquals(
                AppointmentStatus.NO_SHOW,
                response.getStatus()
        );
    }

    @Test
    void shouldReturnDailyAppointmentsSortedByStartTime() {

        LocalDate date =
                LocalDate.of(2026, 9, 19);

        Appointment later =
                createAppointment(
                        date,
                        LocalTime.of(11, 0),
                        AppointmentStatus.CONFIRMED
                );

        Appointment earlier =
                createAppointment(
                        date,
                        LocalTime.of(9, 0),
                        AppointmentStatus.REQUESTED
                );

        when(appointmentRepository
                .findByTenantIdAndAppointmentDate(
                        tenantId,
                        date
                ))
                .thenReturn(List.of(later, earlier));

        List<AppointmentResponse> result =
                appointmentService.getDailyAppointments(
                        tenantId,
                        date
                );

        assertEquals(2, result.size());

        assertEquals(
                LocalTime.of(9, 0),
                result.get(0).getStartTime()
        );

        assertEquals(
                LocalTime.of(11, 0),
                result.get(1).getStartTime()
        );
    }

    @Test
    void shouldReturnCorrectDailySummary() {

        LocalDate date =
                LocalDate.of(2026, 9, 19);

        when(appointmentRepository
                .findByTenantIdAndAppointmentDate(
                        tenantId,
                        date
                ))
                .thenReturn(List.of(
                        createAppointment(
                                date,
                                LocalTime.of(9, 0),
                                AppointmentStatus.REQUESTED
                        ),
                        createAppointment(
                                date,
                                LocalTime.of(10, 0),
                                AppointmentStatus.CONFIRMED
                        ),
                        createAppointment(
                                date,
                                LocalTime.of(11, 0),
                                AppointmentStatus.COMPLETED
                        ),
                        createAppointment(
                                date,
                                LocalTime.of(12, 0),
                                AppointmentStatus.CANCELLED
                        ),
                        createAppointment(
                                date,
                                LocalTime.of(13, 0),
                                AppointmentStatus.NO_SHOW
                        )
                ));

        var result =
                appointmentService.getDailySummary(
                        tenantId,
                        date
                );

        assertEquals(date, result.getDate());
        assertEquals(5, result.getTotal());
        assertEquals(1, result.getRequested());
        assertEquals(1, result.getConfirmed());
        assertEquals(1, result.getCompleted());
        assertEquals(1, result.getCancelled());
        assertEquals(1, result.getNoShow());
    }

    private AppointmentCreateRequest createValidRequest() {

        AppointmentCreateRequest request =
                new AppointmentCreateRequest();

        request.setClinicId(clinicId);
        request.setDoctorId(doctorId);
        request.setPatientId(patientId);

        request.setAppointmentDate(
                LocalDate.now().plusDays(1)
        );

        request.setStartTime(
                LocalTime.of(10, 0)
        );

        request.setEndTime(
                LocalTime.of(10, 30)
        );

        request.setReason(
                "General consultation"
        );

        request.setNotes(
                "Test appointment"
        );

        return request;
    }

    private Clinic mockClinic() {
        return mock(Clinic.class);
    }

    private Doctor mockDoctor() {

        Doctor doctor = mock(Doctor.class);

        when(doctor.getClinicId())
                .thenReturn(clinicId);

        return doctor;
    }

    private Patient mockPatient() {

        Patient patient = mock(Patient.class);

        when(patient.getClinicId())
                .thenReturn(clinicId);

        return patient;
    }

    private DoctorAvailability mockAvailability() {

        DoctorAvailability availability =
                mock(DoctorAvailability.class);

        short day =
                (short) LocalDate.now()
                        .plusDays(1)
                        .getDayOfWeek()
                        .getValue();

        when(availability.getDayOfWeek())
                .thenReturn(day);

        when(availability.getStartTime())
                .thenReturn(LocalTime.of(9, 0));

        when(availability.getEndTime())
                .thenReturn(LocalTime.of(18, 0));

        return availability;
    }

    private Appointment createAppointment(
            LocalDate date,
            LocalTime startTime,
            AppointmentStatus status) {

        Appointment appointment =
                new Appointment();

        appointment.setId(UUID.randomUUID());
        appointment.setTenantId(tenantId);
        appointment.setClinicId(clinicId);
        appointment.setDoctorId(doctorId);
        appointment.setPatientId(patientId);
        appointment.setAppointmentDate(date);
        appointment.setStartTime(startTime);
        appointment.setEndTime(
                startTime.plusMinutes(30)
        );
        appointment.setStatus(status);

        return appointment;
    }
}