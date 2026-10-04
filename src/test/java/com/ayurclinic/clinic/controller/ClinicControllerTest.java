package com.ayurclinic.clinic.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.clinic.dto.ClinicResponse;
import com.ayurclinic.clinic.entity.ClinicStatus;
import com.ayurclinic.clinic.service.ClinicService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.ayurclinic.auth.security.JwtService;
import java.util.UUID;
import com.ayurclinic.auth.security.CustomUserDetailsService;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import com.ayurclinic.clinic.entity.ClinicStatus;

@WebMvcTest(ClinicController.class)
@Import(com.ayurclinic.common.exception.GlobalExceptionHandler.class)
@EnableMethodSecurity
class ClinicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClinicService clinicService;

    @MockBean
    private JwtService jwtService;
    @MockBean
    private CustomUserDetailsService userDetailsService;
    @Test
    void getClinic_whenClinicNotFound_returns404() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
                        )
                ),
                true
        );

        when(clinicService.getClinic(eq(tenantId), eq(clinicId)))
                .thenThrow(new jakarta.persistence.EntityNotFoundException(
                        "Clinic not found"
                ));

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        mockMvc.perform(
                        get("/api/clinics/{clinicId}", clinicId)
                                .with(authentication(authentication))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Clinic not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/clinics/" + clinicId));
    }

    @Test
    void createClinic_whenNameIsBlank_returns400() throws Exception {

        UUID tenantId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
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

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/clinics")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                        {
                            "name": ""
                        }
                        """)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/clinics"))
                .andExpect(jsonPath("$.errors.name").value("must not be blank"));
    }

    @Test
    void createClinic_whenDuplicateName_returns400() throws Exception {

        UUID tenantId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
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

        when(clinicService.createClinic(
                eq(tenantId),
                any(com.ayurclinic.clinic.dto.ClinicCreateRequest.class)
        )).thenThrow(
                new IllegalArgumentException(
                        "Clinic with name 'AyurClinic Pune' already exists"
                )
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/clinics")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "name": "AyurClinic Pune"
                                    }
                                    """)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message")
                        .value("Clinic with name 'AyurClinic Pune' already exists"))
                .andExpect(jsonPath("$.path").value("/api/clinics"));
    }

    @Test
    void createClinic_whenUserIsNotClinicAdmin_returns403() throws Exception {

        UUID tenantId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "doctor@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
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

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .post("/api/clinics")
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "name": "Doctor Clinic"
                                    }
                                    """)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void getClinic_whenClinicBelongsToAnotherTenant_returns404() throws Exception {

        UUID userTenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                userTenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
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

        when(clinicService.getClinic(
                eq(userTenantId),
                eq(clinicId)
        )).thenThrow(
                new jakarta.persistence.EntityNotFoundException(
                        "Clinic not found"
                )
        );

        mockMvc.perform(
                        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                                .get("/api/clinics/{clinicId}", clinicId)
                                .with(authentication(authentication))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Clinic not found"))
                .andExpect(jsonPath("$.path")
                        .value("/api/clinics/" + clinicId));
    }

    @Test
    void getCurrentClinic_returnsClinicForAuthenticatedTenant() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
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

        ClinicResponse response = org.mockito.Mockito.mock(ClinicResponse.class);

        when(response.getId()).thenReturn(clinicId);
        when(response.getTenantId()).thenReturn(tenantId);
        when(response.getName()).thenReturn("AyurClinic Pune");

        when(clinicService.getCurrentTenantClinic(tenantId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/clinics/current")
                                .with(authentication(authentication))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clinicId.toString()))
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.name").value("AyurClinic Pune"));

        verify(clinicService)
                .getCurrentTenantClinic(tenantId);
    }

    @Test
    void updateClinic_whenClinicAdmin_returnsUpdatedClinic() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
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

        ClinicResponse response = org.mockito.Mockito.mock(ClinicResponse.class);

        when(response.getId()).thenReturn(clinicId);
        when(response.getTenantId()).thenReturn(tenantId);
        when(response.getName()).thenReturn("Updated AyurClinic Pune");

        when(clinicService.updateClinic(
                eq(tenantId),
                eq(clinicId),
                any(com.ayurclinic.clinic.dto.ClinicUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        put("/api/clinics/{clinicId}", clinicId)
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "name": "Updated AyurClinic Pune"
                                    }
                                    """)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clinicId.toString()))
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.name").value("Updated AyurClinic Pune"));

        verify(clinicService).updateClinic(
                eq(tenantId),
                eq(clinicId),
                any(com.ayurclinic.clinic.dto.ClinicUpdateRequest.class)
        );
    }

    @Test
    void updateClinicStatus_whenClinicAdmin_returnsUpdatedClinic() throws Exception {

        UUID tenantId = UUID.randomUUID();
        UUID clinicId = UUID.randomUUID();

        CustomUserPrincipal principal = new CustomUserPrincipal(
                UUID.randomUUID(),
                tenantId,
                "admin@test.com",
                "password",
                java.util.List.of(
                        new org.springframework.security.core.authority.SimpleGrantedAuthority(
                                "ROLE_CLINIC_ADMIN"
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

        ClinicResponse response = org.mockito.Mockito.mock(ClinicResponse.class);

        when(response.getId()).thenReturn(clinicId);
        when(response.getTenantId()).thenReturn(tenantId);
        when(response.getName()).thenReturn("AyurClinic Pune");

        when(clinicService.updateClinicStatus(
                eq(tenantId),
                eq(clinicId),
                any(com.ayurclinic.clinic.dto.ClinicStatusUpdateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        patch("/api/clinics/{clinicId}/status", clinicId)
                                .with(authentication(authentication))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                        "status": "INACTIVE"
                                    }
                                    """)
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(clinicId.toString()))
                .andExpect(jsonPath("$.tenantId").value(tenantId.toString()))
                .andExpect(jsonPath("$.name").value("AyurClinic Pune"));

        verify(clinicService).updateClinicStatus(
                eq(tenantId),
                eq(clinicId),
                any(com.ayurclinic.clinic.dto.ClinicStatusUpdateRequest.class)
        );
    }
}