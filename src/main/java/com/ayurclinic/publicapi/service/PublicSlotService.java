package com.ayurclinic.publicapi.service;

import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.enums.AppointmentStatus;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.entity.DoctorAvailability;
import com.ayurclinic.doctor.repository.DoctorAvailabilityRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.publicapi.dto.PublicSlotResponse;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PublicSlotService {

    private final DoctorRepository doctorRepository;
    private final DoctorAvailabilityRepository doctorAvailabilityRepository;
    private final AppointmentRepository appointmentRepository;

    @Value("${ayurclinic.public.clinic-id}")
    private UUID publicClinicId;

    @Transactional(readOnly = true)
    public List<PublicSlotResponse> getAvailableSlots(
            UUID doctorId,
            LocalDate date
    ) {

        Doctor doctor = doctorRepository
                .findByIdAndClinicIdAndStatus(
                        doctorId,
                        publicClinicId,
                        "ACTIVE"
                )
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Public doctor not found"
                        )
                );

        short dayOfWeek = (short) date.getDayOfWeek().getValue();

        List<DoctorAvailability> availabilities =
                doctorAvailabilityRepository
                        .findByDoctorIdAndStatusOrderByDayOfWeek(
                                doctor.getId(),
                                "ACTIVE"
                        );

        List<Appointment> appointments =
                appointmentRepository
                        .findByDoctorIdAndAppointmentDateAndStatusIn(
                                doctor.getId(),
                                date,
                                List.of(
                                        AppointmentStatus.REQUESTED,
                                        AppointmentStatus.CONFIRMED
                                )
                        );

        return availabilities.stream()
                .filter(availability ->
                        availability.getDayOfWeek() == dayOfWeek
                )
                .flatMap(availability ->
                        generateSlots(
                                availability,
                                appointments
                        ).stream()
                )
                .toList();
    }

    private List<PublicSlotResponse> generateSlots(
            DoctorAvailability availability,
            List<Appointment> appointments
    ) {

        LocalTime startTime = availability.getStartTime();
        LocalTime endTime = availability.getEndTime();

        int slotDurationMinutes =
                availability.getSlotDurationMinutes();

        List<PublicSlotResponse> slots = new ArrayList<>();

        LocalTime currentTime = startTime;

        while (!currentTime.plusMinutes(slotDurationMinutes)
                .isAfter(endTime)) {

            LocalTime slotStart = currentTime;

            LocalTime slotEnd =
                    slotStart.plusMinutes(slotDurationMinutes);

            boolean available = appointments.stream()
                    .noneMatch(appointment ->
                            overlaps(
                                    slotStart,
                                    slotEnd,
                                    appointment
                            )
                    );

            slots.add(
                    PublicSlotResponse.builder()
                            .startTime(currentTime)
                            .endTime(slotEnd)
                            .available(available)
                            .build()
            );

            currentTime = slotEnd;
        }

        return slots;
    }

    private boolean overlaps(
            LocalTime slotStart,
            LocalTime slotEnd,
            Appointment appointment
    ) {

        return slotStart.isBefore(appointment.getEndTime())
                && slotEnd.isAfter(appointment.getStartTime());
    }
}