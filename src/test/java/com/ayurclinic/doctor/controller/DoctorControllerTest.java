package com.ayurclinic.doctor.controller;

import com.ayurclinic.auth.security.CustomUserDetailsService;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.auth.security.JwtService;
import com.ayurclinic.common.exception.GlobalExceptionHandler;
import com.ayurclinic.doctor.dto.CreateDoctorRequest;
import com.ayurclinic.doctor.dto.DoctorResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorStatusRequest;
import com.ayurclinic.doctor.service.DoctorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DoctorController.class)
@Import(GlobalExceptionHandler.class)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID tenantId =
            UUID.fromString("7316782b-0d42-4fce-ae6b-4210e0958ead");

    private final UUID clinicId =
            UUID.fromString("38aceb8a-f904-4063-86bb-a01f8caa9100");

    private final UUID doctorId =
            UUID.randomUUID();

    private Authentication testAuthentication() {

        CustomUserPrincipal principal =
                new CustomUserPrincipal(
                        UUID.randomUUID(),
                        tenantId,
                        "admin@ayurclinic.com",
                        "password",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CLINIC_ADMIN"
                                )
                        ),
                        true
                );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    @Test
    void createDoctor_shouldCreateDoctor() throws Exception {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctorId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setFirstName("Dr. Rahul");
        response.setLastName("Sharma");
        response.setQualification("BAMS");
        response.setSpecialization("Panchakarma");
        response.setRegistrationNumber("AYU12345");
        response.setExperienceYears(10);
        response.setStatus("ACTIVE");

        when(doctorService.createDoctor(
                eq(tenantId),
                any(CreateDoctorRequest.class)
        )).thenReturn(response);

        String requestJson = """
                {
                  "clinicId": "%s",
                  "firstName": "Dr. Rahul",
                  "lastName": "Sharma",
                  "qualification": "BAMS",
                  "specialization": "Panchakarma",
                  "registrationNumber": "AYU12345",
                  "experienceYears": 10,
                  "consultationFee": 500.00
                }
                """.formatted(clinicId);

        mockMvc.perform(
                        post("/api/v1/doctors")
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(doctorId.toString()))
                .andExpect(jsonPath("$.tenantId")
                        .value(tenantId.toString()))
                .andExpect(jsonPath("$.clinicId")
                        .value(clinicId.toString()))
                .andExpect(jsonPath("$.firstName")
                        .value("Dr. Rahul"))
                .andExpect(jsonPath("$.qualification")
                        .value("BAMS"))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        verify(doctorService).createDoctor(
                eq(tenantId),
                any(CreateDoctorRequest.class)
        );
    }

    @Test
    void getDoctor_shouldReturnDoctor() throws Exception {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctorId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setFirstName("Dr. Rahul");
        response.setQualification("BAMS");
        response.setStatus("ACTIVE");

        when(doctorService.getDoctor(
                tenantId,
                doctorId
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/doctors/{id}", doctorId)
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(doctorId.toString()))
                .andExpect(jsonPath("$.firstName")
                        .value("Dr. Rahul"))
                .andExpect(jsonPath("$.qualification")
                        .value("BAMS"));

        verify(doctorService).getDoctor(
                tenantId,
                doctorId
        );
    }

    @Test
    void getDoctors_shouldReturnDoctors() throws Exception {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctorId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setFirstName("Dr. Rahul");
        response.setStatus("ACTIVE");

        when(doctorService.getDoctors(tenantId))
                .thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/doctors")
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName")
                        .value("Dr. Rahul"));

        verify(doctorService).getDoctors(tenantId);
        verifyNoMoreInteractions(doctorService);
    }

    @Test
    void getDoctorsByClinic_shouldReturnDoctors() throws Exception {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctorId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setFirstName("Dr. Rahul");
        response.setStatus("ACTIVE");

        when(doctorService.getDoctorsByClinic(
                tenantId,
                clinicId
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/doctors")
                                .param("clinicId", clinicId.toString())
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].clinicId")
                        .value(clinicId.toString()));

        verify(doctorService).getDoctorsByClinic(
                tenantId,
                clinicId
        );
    }

    @Test
    void updateDoctor_shouldUpdateDoctor() throws Exception {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctorId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setFirstName("Dr. Rahul Updated");
        response.setQualification("BAMS MD");
        response.setSpecialization("Ayurveda");
        response.setStatus("ACTIVE");

        when(doctorService.updateDoctor(
                eq(tenantId),
                eq(doctorId),
                any(UpdateDoctorRequest.class)
        )).thenReturn(response);

        String requestJson = """
                {
                  "clinicId": "%s",
                  "firstName": "Dr. Rahul Updated",
                  "lastName": "Sharma",
                  "qualification": "BAMS MD",
                  "specialization": "Ayurveda",
                  "registrationNumber": "AYU12345",
                  "experienceYears": 12,
                  "consultationFee": 750.00
                }
                """.formatted(clinicId);

        mockMvc.perform(
                        put("/api/v1/doctors/{id}", doctorId)
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName")
                        .value("Dr. Rahul Updated"))
                .andExpect(jsonPath("$.qualification")
                        .value("BAMS MD"));

        verify(doctorService).updateDoctor(
                eq(tenantId),
                eq(doctorId),
                any(UpdateDoctorRequest.class)
        );
    }

    @Test
    void updateDoctorStatus_shouldUpdateStatus() throws Exception {

        DoctorResponse response = new DoctorResponse();

        response.setId(doctorId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setFirstName("Dr. Rahul");
        response.setStatus("INACTIVE");

        when(doctorService.updateDoctorStatus(
                eq(tenantId),
                eq(doctorId),
                any(UpdateDoctorStatusRequest.class)
        )).thenReturn(response);

        String requestJson = """
                {
                  "status": "INACTIVE"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/doctors/{id}/status", doctorId)
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("INACTIVE"));

        verify(doctorService).updateDoctorStatus(
                eq(tenantId),
                eq(doctorId),
                any(UpdateDoctorStatusRequest.class)
        );
    }

    @Test
    void createDoctor_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        String requestJson = """
                {
                  "clinicId": "%s",
                  "firstName": "Dr. Rahul"
                }
                """.formatted(clinicId);

        mockMvc.perform(
                        post("/api/v1/doctors")
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(doctorService);
    }
}