package com.ayurclinic.clinic.service;

import com.ayurclinic.clinic.dto.ClinicCreateRequest;
import com.ayurclinic.clinic.dto.ClinicStatusUpdateRequest;
import com.ayurclinic.clinic.dto.ClinicUpdateRequest;
import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.entity.ClinicStatus;
import com.ayurclinic.clinic.repository.ClinicRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicServiceTest {

    @Mock
    private ClinicRepository clinicRepository;

    @InjectMocks
    private ClinicService clinicService;

    @Test
    void createClinic_whenValidRequest_createsClinic() {

        UUID tenantId = UUID.randomUUID();

        ClinicCreateRequest request = new ClinicCreateRequest();
        request.setName("Test AyurClinic");



        when(clinicRepository.existsByTenantIdAndName(
                tenantId,
                "Test AyurClinic"
        )).thenReturn(false);

        when(clinicRepository.save(any(Clinic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = clinicService.createClinic(tenantId, request);

        assertNotNull(response);
        assertEquals("Test AyurClinic", response.getName());

        verify(clinicRepository)
                .save(any(Clinic.class));
    }

    @Test
    void createClinic_whenDuplicateName_throwsBadRequest() {

        UUID tenantId = UUID.randomUUID();

        ClinicCreateRequest request = new ClinicCreateRequest();
        request.setName("AyurClinic Pune");

        when(clinicRepository.existsByTenantIdAndName(
                tenantId,
                "AyurClinic Pune"
        )).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> clinicService.createClinic(tenantId, request)
        );

        assertEquals(
                "Clinic with name 'AyurClinic Pune' already exists",
                exception.getMessage()
        );

        verify(clinicRepository, never())
                .save(any(Clinic.class));
    }
    @Test
    void getClinic_whenClinicBelongsToTenant_returnsClinic() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Clinic clinic = new Clinic();
        clinic.setId(clinicId);
        clinic.setTenantId(tenantId);
        clinic.setName("AyurClinic Pune");

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(java.util.Optional.of(clinic));

        var response = clinicService.getClinic(tenantId, clinicId);

        assertNotNull(response);
        assertEquals(clinicId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals("AyurClinic Pune", response.getName());

        verify(clinicRepository)
                .findByIdAndTenantId(clinicId, tenantId);
    }

    @Test
    void getClinic_whenClinicDoesNotBelongToTenant_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(java.util.Optional.empty());

        jakarta.persistence.EntityNotFoundException exception =
                assertThrows(
                        jakarta.persistence.EntityNotFoundException.class,
                        () -> clinicService.getClinic(tenantId, clinicId)
                );

        assertEquals(
                "Clinic not found",
                exception.getMessage()
        );

        verify(clinicRepository)
                .findByIdAndTenantId(clinicId, tenantId);

        verify(clinicRepository, never())
                .save(any(Clinic.class));
    }

    @Test
    void getCurrentTenantClinic_whenClinicExists_returnsClinic() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Clinic clinic = new Clinic();
        clinic.setId(clinicId);
        clinic.setTenantId(tenantId);
        clinic.setName("AyurClinic Pune");

        when(clinicRepository.findByTenantId(tenantId))
                .thenReturn(java.util.Optional.of(clinic));

        var response = clinicService.getCurrentTenantClinic(tenantId);

        assertNotNull(response);
        assertEquals(clinicId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals("AyurClinic Pune", response.getName());

        verify(clinicRepository)
                .findByTenantId(tenantId);
    }

    @Test
    void getCurrentTenantClinic_whenClinicDoesNotExist_throwsNotFound() {

        UUID tenantId = UUID.randomUUID();

        when(clinicRepository.findByTenantId(tenantId))
                .thenReturn(java.util.Optional.empty());

        jakarta.persistence.EntityNotFoundException exception =
                assertThrows(
                        jakarta.persistence.EntityNotFoundException.class,
                        () -> clinicService.getCurrentTenantClinic(tenantId)
                );

        assertEquals(
                "Clinic not found",
                exception.getMessage()
        );

        verify(clinicRepository)
                .findByTenantId(tenantId);

        verify(clinicRepository, never())
                .save(any(Clinic.class));
    }

    @Test
    void updateClinic_whenClinicExists_updatesClinic() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Clinic clinic = new Clinic();
        clinic.setId(clinicId);
        clinic.setTenantId(tenantId);
        clinic.setName("Old Clinic");

        ClinicUpdateRequest request = new ClinicUpdateRequest();
        request.setName("Updated AyurClinic Pune");

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(java.util.Optional.of(clinic));

        when(clinicRepository.save(any(Clinic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = clinicService.updateClinic(
                tenantId,
                clinicId,
                request
        );

        assertNotNull(response);
        assertEquals(clinicId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals("Updated AyurClinic Pune", response.getName());

        verify(clinicRepository)
                .findByIdAndTenantId(clinicId, tenantId);

        verify(clinicRepository)
                .save(clinic);
    }

    @Test
    void updateClinicStatus_whenClinicExists_updatesStatus() {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        Clinic clinic = new Clinic();
        clinic.setId(clinicId);
        clinic.setTenantId(tenantId);
        clinic.setName("AyurClinic Pune");
        clinic.setStatus(ClinicStatus.ACTIVE);

        ClinicStatusUpdateRequest request = new ClinicStatusUpdateRequest();
        request.setStatus(ClinicStatus.INACTIVE);

        when(clinicRepository.findByIdAndTenantId(
                clinicId,
                tenantId
        )).thenReturn(java.util.Optional.of(clinic));

        when(clinicRepository.save(any(Clinic.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = clinicService.updateClinicStatus(
                tenantId,
                clinicId,
                request
        );

        assertNotNull(response);
        assertEquals(clinicId, response.getId());
        assertEquals(tenantId, response.getTenantId());
        assertEquals(ClinicStatus.INACTIVE, response.getStatus());

        verify(clinicRepository)
                .findByIdAndTenantId(clinicId, tenantId);

        verify(clinicRepository)
                .save(clinic);
    }
}