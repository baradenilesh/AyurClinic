package com.ayurclinic.patient.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.common.exception.GlobalExceptionHandler;
import com.ayurclinic.patient.dto.PatientHistoryRequest;
import com.ayurclinic.patient.dto.PatientHistoryResponse;
import com.ayurclinic.patient.service.PatientHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PatientHistoryController.class)
@Import(GlobalExceptionHandler.class)
class PatientHistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientHistoryService patientHistoryService;

    @MockBean
    private com.ayurclinic.auth.security.JwtService jwtService;

    @MockBean
    private com.ayurclinic.auth.security.CustomUserDetailsService customUserDetailsService;

    private final UUID tenantId =
            UUID.fromString("7316782b-0d42-4fce-ae6b-4210e0958ead");

    private final UUID patientId =
            UUID.randomUUID();

    private Authentication testAuthentication() {

        CustomUserPrincipal principal =
                new CustomUserPrincipal(
                        UUID.randomUUID(),
                        tenantId,
                        "admin@ayurclinic.com",
                        "password",
                        List.of(new SimpleGrantedAuthority("ROLE_CLINIC_ADMIN")),
                        true
                );

        return new UsernamePasswordAuthenticationToken(
                principal,
                null,
                principal.getAuthorities()
        );
    }

    @Test
    void addHistory_shouldCreateHistory() throws Exception {

        PatientHistoryResponse response =
                new PatientHistoryResponse();

        UUID historyId = UUID.randomUUID();

        response.setId(historyId);
        response.setPatientId(patientId);
        response.setHistoryType("CONSULTATION");
        response.setTitle("Initial Consultation");
        response.setDescription("Patient consultation completed.");

        when(patientHistoryService.addHistory(
                eq(tenantId),
                eq(patientId),
                any(PatientHistoryRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/patients/{patientId}/history", patientId)
                                .with(authentication(testAuthentication())).with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "historyType": "CONSULTATION",
                                      "title": "Initial Consultation",
                                      "description": "Patient consultation completed."
                                    }
                                    """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(historyId.toString()))
                .andExpect(jsonPath("$.patientId").value(patientId.toString()))
                .andExpect(jsonPath("$.historyType").value("CONSULTATION"))
                .andExpect(jsonPath("$.title").value("Initial Consultation"));

        verify(patientHistoryService).addHistory(
                eq(tenantId),
                eq(patientId),
                any(PatientHistoryRequest.class)
        );
    }

    @Test
    void getHistory_shouldReturnPatientHistory() throws Exception {

        PatientHistoryResponse response =
                new PatientHistoryResponse();

        UUID historyId = UUID.randomUUID();

        response.setId(historyId);
        response.setPatientId(patientId);
        response.setHistoryType("CONSULTATION");
        response.setTitle("Initial Consultation");
        response.setDescription("Patient consultation completed.");

        when(patientHistoryService.getHistory(
                eq(tenantId),
                eq(patientId)
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get(
                                "/api/v1/patients/{patientId}/history",
                                patientId
                        )
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].id")
                                .value(historyId.toString())
                )
                .andExpect(
                        jsonPath("$[0].patientId")
                                .value(patientId.toString())
                )
                .andExpect(
                        jsonPath("$[0].historyType")
                                .value("CONSULTATION")
                )
                .andExpect(
                        jsonPath("$[0].title")
                                .value("Initial Consultation")
                );

        verify(patientHistoryService).getHistory(
                tenantId,
                patientId
        );
    }

    @Test
    void contextLoads() {
    }
}