package com.ayurclinic.consultation.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.consultation.dto.ConsultationCreateRequest;
import com.ayurclinic.consultation.dto.ConsultationResponse;
import com.ayurclinic.consultation.dto.ConsultationUpdateRequest;
import com.ayurclinic.consultation.service.ConsultationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ConsultationControllerTest {

    @Mock
    private ConsultationService consultationService;

    @InjectMocks
    private ConsultationController consultationController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID tenantId;
    private UUID userId;
    private UUID consultationId;
    private UUID appointmentId;
    private UUID patientId;
    private UUID doctorId;
    private UUID clinicId;

    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();

        objectMapper.registerModule(new JavaTimeModule());

        objectMapper.disable(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS
        );

        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        consultationId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        doctorId = UUID.randomUUID();
        clinicId = UUID.randomUUID();

        principal = new CustomUserPrincipal(
                userId,
                tenantId,
                "doctor@ayurclinic.com",
                "password",
                List.of(
                        new SimpleGrantedAuthority(
                                "ROLE_DOCTOR"
                        )
                ),
                true
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        var securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(
                securityContext
        );

        mockMvc = MockMvcBuilders
                .standaloneSetup(
                        consultationController
                )
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldCreateConsultation() throws Exception {

        ConsultationCreateRequest request =
                new ConsultationCreateRequest();

        request.setAppointmentId(appointmentId);
        request.setChiefComplaint(
                "Headache"
        );
        request.setSymptoms(
                "Headache and fatigue"
        );
        request.setClinicalFindings(
                "Mild fatigue"
        );
        request.setDiagnosis(
                "Stress-related headache"
        );
        request.setTreatmentPlan(
                "Ayurvedic treatment"
        );
        request.setDoctorNotes(
                "Follow up after two weeks"
        );

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setAppointmentId(appointmentId);
        response.setPatientId(patientId);
        response.setDoctorId(doctorId);
        response.setConsultationDate(
                LocalDate.of(2026, 10, 2)
        );
        response.setChiefComplaint(
                "Headache"
        );
        response.setStatus(
                "IN_PROGRESS"
        );

        when(
                consultationService.createConsultation(
                        eq(tenantId),
                        any(ConsultationCreateRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/consultations")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        consultationId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_PROGRESS")
                )
                .andExpect(
                        jsonPath("$.chiefComplaint")
                                .value("Headache")
                );

        verify(
                consultationService
        ).createConsultation(
                eq(tenantId),
                any(ConsultationCreateRequest.class)
        );
    }

    @Test
    void shouldGetConsultationById() throws Exception {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setTenantId(tenantId);
        response.setAppointmentId(appointmentId);
        response.setPatientId(patientId);
        response.setDoctorId(doctorId);
        response.setStatus("IN_PROGRESS");

        when(
                consultationService.getConsultationById(
                        tenantId,
                        consultationId
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/consultations/"
                                        + consultationId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        consultationId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.status")
                                .value("IN_PROGRESS")
                );

        verify(
                consultationService
        ).getConsultationById(
                tenantId,
                consultationId
        );
    }

    @Test
    void shouldGetConsultationByAppointment() throws Exception {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setAppointmentId(appointmentId);
        response.setStatus("IN_PROGRESS");

        when(
                consultationService.getByAppointmentId(
                        tenantId,
                        appointmentId
                )
        ).thenReturn(response);

        mockMvc.perform(
                        get(
                                "/api/v1/consultations/appointment/"
                                        + appointmentId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.appointmentId")
                                .value(
                                        appointmentId.toString()
                                )
                );

        verify(
                consultationService
        ).getByAppointmentId(
                tenantId,
                appointmentId
        );
    }

    @Test
    void shouldGetPatientConsultations() throws Exception {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setPatientId(patientId);
        response.setStatus("COMPLETED");

        when(
                consultationService.getPatientConsultations(
                        tenantId,
                        patientId
                )
        ).thenReturn(List.of(response));

        mockMvc.perform(
                        get(
                                "/api/v1/consultations/patient/"
                                        + patientId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].patientId")
                                .value(
                                        patientId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$[0].status")
                                .value("COMPLETED")
                );

        verify(
                consultationService
        ).getPatientConsultations(
                tenantId,
                patientId
        );
    }

    @Test
    void shouldGetDoctorConsultations() throws Exception {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setDoctorId(doctorId);
        response.setStatus("IN_PROGRESS");

        when(
                consultationService.getDoctorConsultations(
                        tenantId,
                        doctorId
                )
        ).thenReturn(List.of(response));

        mockMvc.perform(
                        get(
                                "/api/v1/consultations/doctor/"
                                        + doctorId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].doctorId")
                                .value(
                                        doctorId.toString()
                                )
                );

        verify(
                consultationService
        ).getDoctorConsultations(
                tenantId,
                doctorId
        );
    }

    @Test
    void shouldGetClinicConsultations() throws Exception {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setClinicId(clinicId);
        response.setStatus("COMPLETED");

        when(
                consultationService.getClinicConsultations(
                        tenantId,
                        clinicId
                )
        ).thenReturn(List.of(response));

        mockMvc.perform(
                        get(
                                "/api/v1/consultations/clinic/"
                                        + clinicId
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.length()")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$[0].clinicId")
                                .value(
                                        clinicId.toString()
                                )
                );

        verify(
                consultationService
        ).getClinicConsultations(
                tenantId,
                clinicId
        );
    }

    @Test
    void shouldUpdateConsultation() throws Exception {

        ConsultationUpdateRequest request =
                new ConsultationUpdateRequest();

        request.setChiefComplaint(
                "Updated headache"
        );
        request.setSymptoms(
                "Updated symptoms"
        );
        request.setClinicalFindings(
                "Updated findings"
        );
        request.setDiagnosis(
                "Updated diagnosis"
        );
        request.setTreatmentPlan(
                "Updated treatment"
        );
        request.setDoctorNotes(
                "Updated notes"
        );

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setStatus("IN_PROGRESS");
        response.setDiagnosis(
                "Updated diagnosis"
        );

        when(
                consultationService.updateConsultation(
                        eq(tenantId),
                        eq(consultationId),
                        any(ConsultationUpdateRequest.class)
                )
        ).thenReturn(response);

        mockMvc.perform(
                        put(
                                "/api/v1/consultations/"
                                        + consultationId
                        )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.id")
                                .value(
                                        consultationId.toString()
                                )
                )
                .andExpect(
                        jsonPath("$.diagnosis")
                                .value(
                                        "Updated diagnosis"
                                )
                );

        verify(
                consultationService
        ).updateConsultation(
                eq(tenantId),
                eq(consultationId),
                any(ConsultationUpdateRequest.class)
        );
    }

    @Test
    void shouldCompleteConsultation() throws Exception {

        ConsultationResponse response =
                new ConsultationResponse();

        response.setId(consultationId);
        response.setStatus("COMPLETED");

        when(
                consultationService.completeConsultation(
                        tenantId,
                        consultationId
                )
        ).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/consultations/"
                                        + consultationId
                                        + "/complete"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.status")
                                .value("COMPLETED")
                );

        verify(
                consultationService
        ).completeConsultation(
                tenantId,
                consultationId
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}