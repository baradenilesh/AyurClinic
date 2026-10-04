package com.ayurclinic.doctor.service;

import com.ayurclinic.doctor.dto.CreateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.DoctorAvailabilityResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityStatusRequest;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.entity.DoctorAvailability;
import com.ayurclinic.doctor.repository.DoctorAvailabilityRepository;
import com.ayurclinic.doctor.repository.DoctorRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DoctorAvailabilityServiceTest {

    @Mock
    private DoctorAvailabilityRepository doctorAvailabilityRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private DoctorAvailabilityService doctorAvailabilityService;

    @Test
    void createAvailability_whenValidRequest_createsAvailabilityForTenant() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(doctorId);
        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(13, 0));
        request.setSlotDurationMinutes(30);

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(new Doctor()));

        when(doctorAvailabilityRepository
                .existsByTenantIdAndDoctorIdAndDayOfWeek(
                        tenantId,
                        doctorId,
                        (short) 1
                ))
                .thenReturn(false);

        DoctorAvailability savedAvailability =
                new DoctorAvailability();

        savedAvailability.setId(UUID.randomUUID());
        savedAvailability.setTenantId(tenantId);
        savedAvailability.setDoctorId(doctorId);
        savedAvailability.setDayOfWeek((short) 1);
        savedAvailability.setStartTime(
                LocalTime.of(9, 0)
        );
        savedAvailability.setEndTime(
                LocalTime.of(13, 0)
        );
        savedAvailability.setSlotDurationMinutes(30);
        savedAvailability.setStatus("ACTIVE");

        when(doctorAvailabilityRepository.save(
                any(DoctorAvailability.class)
        )).thenReturn(savedAvailability);

        DoctorAvailabilityResponse response =
                doctorAvailabilityService.createAvailability(
                        tenantId,
                        request
                );

        assertNotNull(response);
        assertEquals(tenantId, response.getTenantId());
        assertEquals(doctorId, response.getDoctorId());
        assertEquals(
                (short) 1,
                response.getDayOfWeek()
        );
        assertEquals(
                LocalTime.of(9, 0),
                response.getStartTime()
        );
        assertEquals(
                LocalTime.of(13, 0),
                response.getEndTime()
        );
        assertEquals(
                30,
                response.getSlotDurationMinutes()
        );
        assertEquals(
                "ACTIVE",
                response.getStatus()
        );

        verify(doctorAvailabilityRepository)
                .save(any(DoctorAvailability.class));
    }

    @Test
    void createAvailability_whenDoctorBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(doctorId);
        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(13, 0));
        request.setSlotDurationMinutes(30);

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorAvailabilityService.createAvailability(
                        tenantId,
                        request
                )
        );

        verify(doctorAvailabilityRepository, never())
                .save(any(DoctorAvailability.class));
    }

    @Test
    void createAvailability_whenDayAlreadyExists_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(doctorId);
        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(13, 0));
        request.setSlotDurationMinutes(30);

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(new Doctor()));

        when(doctorAvailabilityRepository
                .existsByTenantIdAndDoctorIdAndDayOfWeek(
                        tenantId,
                        doctorId,
                        (short) 1
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorAvailabilityService
                                .createAvailability(
                                        tenantId,
                                        request
                                )
                );

        assertEquals(
                "Availability already exists for this doctor and day",
                exception.getMessage()
        );

        verify(doctorAvailabilityRepository, never())
                .save(any(DoctorAvailability.class));
    }

    @Test
    void createAvailability_whenStartTimeIsAfterEndTime_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(doctorId);
        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(14, 0));
        request.setEndTime(LocalTime.of(10, 0));
        request.setSlotDurationMinutes(30);

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(new Doctor()));

        when(doctorAvailabilityRepository
                .existsByTenantIdAndDoctorIdAndDayOfWeek(
                        tenantId,
                        doctorId,
                        (short) 1
                ))
                .thenReturn(false);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorAvailabilityService
                                .createAvailability(
                                        tenantId,
                                        request
                                )
                );

        assertEquals(
                "Start time must be before end time",
                exception.getMessage()
        );

        verify(doctorAvailabilityRepository, never())
                .save(any(DoctorAvailability.class));
    }

    @Test
    void getAvailability_whenDoctorBelongsToTenant_returnsAvailability() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(UUID.randomUUID());
        availability.setTenantId(tenantId);
        availability.setDoctorId(doctorId);
        availability.setDayOfWeek((short) 1);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(13, 0));
        availability.setSlotDurationMinutes(30);
        availability.setStatus("ACTIVE");

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.of(new Doctor()));

        when(doctorAvailabilityRepository
                .findByTenantIdAndDoctorIdOrderByDayOfWeek(
                        tenantId,
                        doctorId
                ))
                .thenReturn(List.of(availability));

        List<DoctorAvailabilityResponse> response =
                doctorAvailabilityService.getAvailability(
                        tenantId,
                        doctorId
                );

        assertEquals(1, response.size());
        assertEquals(
                doctorId,
                response.get(0).getDoctorId()
        );
        assertEquals(
                (short) 1,
                response.get(0).getDayOfWeek()
        );
        assertEquals(
                LocalTime.of(9, 0),
                response.get(0).getStartTime()
        );
    }

    @Test
    void getAvailability_whenDoctorBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        when(doctorRepository.findByIdAndTenantId(
                doctorId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorAvailabilityService.getAvailability(
                        tenantId,
                        doctorId
                )
        );

        verify(
                doctorAvailabilityRepository,
                never()
        ).findByTenantIdAndDoctorIdOrderByDayOfWeek(
                any(UUID.class),
                any(UUID.class)
        );
    }

    @Test
    void getAvailabilityById_whenAvailabilityBelongsToTenant_returnsAvailability() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(availabilityId);
        availability.setTenantId(tenantId);
        availability.setDoctorId(doctorId);
        availability.setDayOfWeek((short) 2);
        availability.setStartTime(LocalTime.of(10, 0));
        availability.setEndTime(LocalTime.of(14, 0));
        availability.setSlotDurationMinutes(20);
        availability.setStatus("ACTIVE");

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.of(availability));

        DoctorAvailabilityResponse response =
                doctorAvailabilityService.getAvailabilityById(
                        tenantId,
                        availabilityId
                );

        assertEquals(
                availabilityId,
                response.getId()
        );
        assertEquals(
                tenantId,
                response.getTenantId()
        );
        assertEquals(
                doctorId,
                response.getDoctorId()
        );
        assertEquals(
                (short) 2,
                response.getDayOfWeek()
        );
    }

    @Test
    void getAvailabilityById_whenAvailabilityBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorAvailabilityService.getAvailabilityById(
                        tenantId,
                        availabilityId
                )
        );
    }

    @Test
    void updateAvailability_whenValidRequest_updatesAvailability() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(availabilityId);
        availability.setTenantId(tenantId);
        availability.setDoctorId(doctorId);
        availability.setDayOfWeek((short) 1);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(13, 0));
        availability.setSlotDurationMinutes(30);
        availability.setStatus("ACTIVE");

        UpdateDoctorAvailabilityRequest request =
                new UpdateDoctorAvailabilityRequest();

        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(15, 0));
        request.setSlotDurationMinutes(20);

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.of(availability));

        when(doctorAvailabilityRepository.save(
                availability
        )).thenReturn(availability);

        DoctorAvailabilityResponse response =
                doctorAvailabilityService.updateAvailability(
                        tenantId,
                        availabilityId,
                        request
                );

        assertEquals(
                LocalTime.of(10, 0),
                response.getStartTime()
        );
        assertEquals(
                LocalTime.of(15, 0),
                response.getEndTime()
        );
        assertEquals(
                20,
                response.getSlotDurationMinutes()
        );

        verify(doctorAvailabilityRepository)
                .save(availability);
    }

    @Test
    void updateAvailability_whenAvailabilityBelongsToAnotherTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();

        UpdateDoctorAvailabilityRequest request =
                new UpdateDoctorAvailabilityRequest();

        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(13, 0));
        request.setSlotDurationMinutes(30);

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> doctorAvailabilityService.updateAvailability(
                        tenantId,
                        availabilityId,
                        request
                )
        );

        verify(doctorAvailabilityRepository, never())
                .save(any(DoctorAvailability.class));
    }

    @Test
    void updateAvailability_whenChangingToExistingDay_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(availabilityId);
        availability.setTenantId(tenantId);
        availability.setDoctorId(doctorId);
        availability.setDayOfWeek((short) 1);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(13, 0));
        availability.setSlotDurationMinutes(30);

        UpdateDoctorAvailabilityRequest request =
                new UpdateDoctorAvailabilityRequest();

        request.setDayOfWeek((short) 2);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(14, 0));
        request.setSlotDurationMinutes(30);

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.of(availability));

        when(doctorAvailabilityRepository
                .existsByTenantIdAndDoctorIdAndDayOfWeekAndIdNot(
                        tenantId,
                        doctorId,
                        (short) 2,
                        availabilityId
                ))
                .thenReturn(true);

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorAvailabilityService
                                .updateAvailability(
                                        tenantId,
                                        availabilityId,
                                        request
                                )
                );

        assertEquals(
                "Availability already exists for this doctor and day",
                exception.getMessage()
        );

        verify(doctorAvailabilityRepository, never())
                .save(any(DoctorAvailability.class));
    }

    @Test
    void updateAvailability_whenStartTimeIsAfterEndTime_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(availabilityId);
        availability.setTenantId(tenantId);
        availability.setDoctorId(doctorId);
        availability.setDayOfWeek((short) 1);

        UpdateDoctorAvailabilityRequest request =
                new UpdateDoctorAvailabilityRequest();

        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(15, 0));
        request.setEndTime(LocalTime.of(10, 0));
        request.setSlotDurationMinutes(30);

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.of(availability));

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorAvailabilityService
                                .updateAvailability(
                                        tenantId,
                                        availabilityId,
                                        request
                                )
                );

        assertEquals(
                "Start time must be before end time",
                exception.getMessage()
        );

        verify(doctorAvailabilityRepository, never())
                .save(any(DoctorAvailability.class));
    }

    @Test
    void updateAvailabilityStatus_whenValidStatus_updatesStatus() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();
        UUID doctorId = UUID.randomUUID();

        DoctorAvailability availability =
                new DoctorAvailability();

        availability.setId(availabilityId);
        availability.setTenantId(tenantId);
        availability.setDoctorId(doctorId);
        availability.setDayOfWeek((short) 1);
        availability.setStartTime(LocalTime.of(9, 0));
        availability.setEndTime(LocalTime.of(13, 0));
        availability.setSlotDurationMinutes(30);
        availability.setStatus("ACTIVE");

        UpdateDoctorAvailabilityStatusRequest request =
                new UpdateDoctorAvailabilityStatusRequest();

        request.setStatus("INACTIVE");

        when(doctorAvailabilityRepository.findByIdAndTenantId(
                availabilityId,
                tenantId
        )).thenReturn(Optional.of(availability));

        when(doctorAvailabilityRepository.save(
                availability
        )).thenReturn(availability);

        DoctorAvailabilityResponse response =
                doctorAvailabilityService.updateAvailabilityStatus(
                        tenantId,
                        availabilityId,
                        request
                );

        assertEquals(
                "INACTIVE",
                response.getStatus()
        );

        verify(doctorAvailabilityRepository)
                .save(availability);
    }

    @Test
    void updateAvailabilityStatus_whenInvalidStatus_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();
        UUID availabilityId = UUID.randomUUID();

        UpdateDoctorAvailabilityStatusRequest request =
                new UpdateDoctorAvailabilityStatusRequest();

        request.setStatus("DELETED");

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> doctorAvailabilityService
                                .updateAvailabilityStatus(
                                        tenantId,
                                        availabilityId,
                                        request
                                )
                );

        assertEquals(
                "Status must be ACTIVE or INACTIVE",
                exception.getMessage()
        );

        verifyNoInteractions(doctorAvailabilityRepository);
    }

    @Test
    void createAvailability_whenTenantIdIsNull_throwsBadRequest() {

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(UUID.randomUUID());
        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(13, 0));
        request.setSlotDurationMinutes(30);

        assertThrows(
                IllegalArgumentException.class,
                () -> doctorAvailabilityService.createAvailability(
                        null,
                        request
                )
        );

        verifyNoInteractions(
                doctorAvailabilityRepository,
                doctorRepository
        );
    }
}

