package com.ayurclinic.publicapi.service;

import com.ayurclinic.appointment.dto.AppointmentCreateRequest;
import com.ayurclinic.appointment.dto.AppointmentResponse;
import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.appointment.service.AppointmentService;
import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.repository.DoctorAvailabilityRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.service.PatientService;
import com.ayurclinic.publicapi.dto.PublicBookingRequest;
import com.ayurclinic.publicapi.dto.PublicBookingResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicBookingService {

    private final PublicClinicService publicClinicService;
    private final DoctorRepository doctorRepository;
    private final PatientService patientService;
    private final AppointmentService appointmentService;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Transactional(readOnly = true)
    public Doctor validatePublicDoctor(UUID doctorId) {

        Clinic clinic = publicClinicService.getPublicClinicEntity();

        return doctorRepository
                .findByIdAndClinicIdAndStatus(
                        doctorId,
                        clinic.getId(),
                        "ACTIVE"
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Public doctor not found"
                        )
                );
    }

    @Transactional(readOnly = true)
    public LocalTime calculateEndTime(
            Doctor doctor,
            LocalDate appointmentDate,
            LocalTime startTime
    ) {

        if (appointmentDate.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Appointment date cannot be in the past"
            );
        }

        short dayOfWeek =
                (short) appointmentDate.getDayOfWeek().getValue();

        var availabilities =
                doctorAvailabilityRepository
                        .findByTenantIdAndDoctorIdAndStatusOrderByDayOfWeek(
                                doctor.getTenantId(),
                                doctor.getId(),
                                "ACTIVE"
                        );

        var availability = availabilities.stream()
                .filter(a -> a.getDayOfWeek().equals(dayOfWeek))
                .filter(a ->
                        !startTime.isBefore(a.getStartTime())
                                && startTime.isBefore(a.getEndTime())
                )
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Doctor is not available at the requested time"
                        )
                );

        int slotDuration =
                availability.getSlotDurationMinutes();

        long minutesFromStart =
                java.time.Duration.between(
                        availability.getStartTime(),
                        startTime
                ).toMinutes();

        if (minutesFromStart % slotDuration != 0) {
            throw new IllegalArgumentException(
                    "Requested time is not a valid appointment slot"
            );
        }

        LocalTime endTime =
                startTime.plusMinutes(slotDuration);

        if (endTime.isAfter(availability.getEndTime())) {
            throw new IllegalArgumentException(
                    "Requested appointment exceeds doctor availability"
            );
        }

        return endTime;
    }

    @Transactional
    public Patient getOrCreatePatient(
            UUID tenantId,
            UUID clinicId,
            String patientName,
            String mobile,
            String email
    ) {
        Patient existingPatient =
                patientService.findPatientByMobile(
                        tenantId,
                        mobile
                );

        if (existingPatient != null) {
            return existingPatient;
        }

        return patientService.createPatientForPublicBooking(
                tenantId,
                clinicId,
                patientName,
                mobile,
                email
        );
    }

    @Transactional
    public AppointmentResponse createAppointment(
            UUID tenantId,
            UUID clinicId,
            UUID doctorId,
            Patient patient,
            LocalDate appointmentDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
        AppointmentCreateRequest request =
                new AppointmentCreateRequest();

        request.setClinicId(clinicId);
        request.setDoctorId(doctorId);
        request.setPatientId(patient.getId());
        request.setAppointmentDate(appointmentDate);
        request.setStartTime(startTime);
        request.setEndTime(endTime);

        AppointmentResponse response =
                appointmentService.createAppointment(
                        tenantId,
                        request
                );

        String bookingReference = generateBookingReference();

        Appointment appointment =
                appointmentRepository
                        .findByIdAndTenantId(
                                response.getId(),
                                tenantId
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Created appointment not found"
                                )
                        );

        appointment.setBookingReference(bookingReference);

        appointmentRepository.save(appointment);

        return response;
    }

    private String generateBookingReference() {
        String token = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        return "AC-" + token;
    }

    @Transactional
    public PublicBookingResponse createBooking(
            PublicBookingRequest request
    ) {
        // 1. Validate public doctor
        Doctor doctor = validatePublicDoctor(request.doctorId());

        // 2. Get public clinic
        Clinic clinic = publicClinicService.getPublicClinicEntity();

        // 3. Calculate and validate appointment end time
        LocalTime endTime = calculateEndTime(
                doctor,
                request.appointmentDate(),
                request.startTime()
        );

        // 4. Get existing patient or create a new one
        Patient patient = getOrCreatePatient(
                clinic.getTenantId(),
                clinic.getId(),
                request.patientName(),
                request.mobile(),
                request.email()
        );

        // 5. Create appointment
        AppointmentResponse appointmentResponse =
                createAppointment(
                        clinic.getTenantId(),
                        clinic.getId(),
                        doctor.getId(),
                        patient,
                        request.appointmentDate(),
                        request.startTime(),
                        endTime
                );

        // 6. Get persisted appointment
        Appointment appointment =
                appointmentRepository
                        .findByIdAndTenantId(
                                appointmentResponse.getId(),
                                clinic.getTenantId()
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Created appointment not found"
                                )
                        );

        // 7. Return safe public response
        return PublicBookingResponse.builder()
                .bookingReference(appointment.getBookingReference())
                .doctorName(doctor.getFirstName()+" "+doctor.getLastName() )
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public PublicBookingResponse getBookingByReference(
            String bookingReference
    ) {
        Appointment appointment =
                appointmentRepository
                        .findByBookingReference(bookingReference)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Booking not found"
                                )
                        );

        Doctor doctor =
                doctorRepository
                        .findByIdAndClinicIdAndStatus(
                                appointment.getDoctorId(),
                                appointment.getClinicId(),
                                "ACTIVE"
                        )
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Doctor not found"
                                )
                        );

        return PublicBookingResponse.builder()
                .bookingReference(appointment.getBookingReference())
                .doctorName(doctor.getFirstName()+" "+doctor.getLastName() )
                .appointmentDate(appointment.getAppointmentDate())
                .startTime(appointment.getStartTime())
                .endTime(appointment.getEndTime())
                .status(appointment.getStatus().name())
                .build();
    }
}