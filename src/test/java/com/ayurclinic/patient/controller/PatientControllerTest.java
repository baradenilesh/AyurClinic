package com.ayurclinic.patient.controller;

import com.ayurclinic.auth.security.CustomUserDetailsService;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.auth.security.JwtService;
import com.ayurclinic.common.exception.GlobalExceptionHandler;
import com.ayurclinic.patient.dto.CreatePatientRequest;
import com.ayurclinic.patient.dto.PatientResponse;
import com.ayurclinic.patient.dto.UpdatePatientRequest;
import com.ayurclinic.patient.dto.UpdatePatientStatusRequest;
import com.ayurclinic.patient.service.PatientService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PatientController.class)
@Import(GlobalExceptionHandler.class)
class PatientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID tenantId =
            UUID.fromString("7316782b-0d42-4fce-ae6b-4210e0958ead");

    private final UUID clinicId =
            UUID.fromString("38aceb8a-f904-4063-86bb-a01f8caa9100");

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
    void createPatient_shouldCreatePatient() throws Exception {

        PatientResponse response = new PatientResponse();

        response.setId(patientId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setPatientNumber("PAT-000001");
        response.setFirstName("Rahul");
        response.setLastName("Sharma");
        response.setMobile("9876543210");
        response.setStatus("ACTIVE");

        when(patientService.createPatient(
                eq(tenantId),
                any(CreatePatientRequest.class)
        )).thenReturn(response);

        String requestJson = """
                {
                  "clinicId": "%s",
                  "firstName": "Rahul",
                  "lastName": "Sharma",
                  "mobile": "9876543210"
                }
                """.formatted(clinicId);

        mockMvc.perform(
                        post("/api/v1/patients")
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.patientNumber").value("PAT-000001"))
                .andExpect(jsonPath("$.firstName").value("Rahul"))
                .andExpect(jsonPath("$.mobile").value("9876543210"));

        verify(patientService).createPatient(
                eq(tenantId),
                any(CreatePatientRequest.class)
        );
    }

    @Test
    void getPatient_shouldReturnPatient() throws Exception {

        PatientResponse response = new PatientResponse();

        response.setId(patientId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setPatientNumber("PAT-000001");
        response.setFirstName("Rahul");
        response.setMobile("9876543210");
        response.setStatus("ACTIVE");

        when(patientService.getPatient(
                tenantId,
                patientId
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/patients/{id}", patientId)
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientId.toString()))
                .andExpect(jsonPath("$.firstName").value("Rahul"));

        verify(patientService).getPatient(
                tenantId,
                patientId
        );
    }

    @Test
    void searchPatients_shouldReturnPatients() throws Exception {

        PatientResponse response = new PatientResponse();

        response.setId(patientId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setPatientNumber("PAT-000001");
        response.setFirstName("Rahul");
        response.setMobile("9876543210");
        response.setStatus("ACTIVE");

        when(patientService.searchPatients(
                eq(tenantId),
                eq("Rahul"),
                isNull(),
                isNull()
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/patients")
                                .param("name", "Rahul")
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].firstName").value("Rahul"));

        verify(patientService).searchPatients(
                tenantId,
                "Rahul",
                null,
                null
        );
    }

    @Test
    void updatePatient_shouldUpdatePatient() throws Exception {

        PatientResponse response = new PatientResponse();

        response.setId(patientId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setPatientNumber("PAT-000001");
        response.setFirstName("Rahul Updated");
        response.setMobile("9999999999");
        response.setStatus("ACTIVE");

        when(patientService.updatePatient(
                eq(tenantId),
                eq(patientId),
                any(UpdatePatientRequest.class)
        )).thenReturn(response);

        String requestJson = """
                {
                  "clinicId": "%s",
                  "firstName": "Rahul Updated",
                  "lastName": "Sharma",
                  "mobile": "9999999999"
                }
                """.formatted(clinicId);

        mockMvc.perform(
                        put("/api/v1/patients/{id}", patientId)
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Rahul Updated"))
                .andExpect(jsonPath("$.mobile").value("9999999999"));

        verify(patientService).updatePatient(
                eq(tenantId),
                eq(patientId),
                any(UpdatePatientRequest.class)
        );
    }

    @Test
    void updatePatientStatus_shouldUpdateStatus() throws Exception {

        PatientResponse response = new PatientResponse();

        response.setId(patientId);
        response.setTenantId(tenantId);
        response.setClinicId(clinicId);
        response.setPatientNumber("PAT-000001");
        response.setFirstName("Rahul");
        response.setMobile("9876543210");
        response.setStatus("INACTIVE");

        when(patientService.updatePatientStatus(
                eq(tenantId),
                eq(patientId),
                any(UpdatePatientStatusRequest.class)
        )).thenReturn(response);

        String requestJson = """
                {
                  "status": "INACTIVE"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/patients/{id}/status", patientId)
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        verify(patientService).updatePatientStatus(
                eq(tenantId),
                eq(patientId),
                any(UpdatePatientStatusRequest.class)
        );
    }

    @Test
    void createPatient_withoutAuthentication_shouldReturnUnauthorized()
            throws Exception {

        String requestJson = """
                {
                  "clinicId": "%s",
                  "firstName": "Rahul",
                  "mobile": "9876543210"
                }
                """.formatted(clinicId);

        mockMvc.perform(
                        post("/api/v1/patients")
                                .with(csrf())
                                .contentType("application/json")
                                .content(requestJson)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(patientService);
    }

}