package com.ayurclinic.doctor.controller;

import com.ayurclinic.auth.security.CustomUserDetailsService;
import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.auth.security.JwtService;
import com.ayurclinic.common.exception.GlobalExceptionHandler;
import com.ayurclinic.doctor.dto.CreateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.DoctorAvailabilityResponse;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityRequest;
import com.ayurclinic.doctor.dto.UpdateDoctorAvailabilityStatusRequest;
import com.ayurclinic.doctor.service.DoctorAvailabilityService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DoctorAvailabilityController.class)
@Import(GlobalExceptionHandler.class)
class DoctorAvailabilityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DoctorAvailabilityService doctorAvailabilityService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    private final UUID tenantId =
            UUID.fromString("7316782b-0d42-4fce-ae6b-4210e0958ead");

    private final UUID doctorId =
            UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final UUID availabilityId =
            UUID.fromString("22222222-2222-2222-2222-222222222222");

    private Authentication testAuthentication() {

        CustomUserPrincipal principal =
                new CustomUserPrincipal(
                        UUID.randomUUID(),
                        tenantId,
                        "admin@ayurclinic.com",
                        "password",
                        List.of(
                                new SimpleGrantedAuthority(
                                        "ROLE_CLINIC_ADMIN")
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
    void createAvailability_shouldReturn201() throws Exception {

        DoctorAvailabilityResponse response =
                createResponse();

        when(doctorAvailabilityService.createAvailability(
                eq(tenantId),
                any(CreateDoctorAvailabilityRequest.class)
        )).thenReturn(response);

        CreateDoctorAvailabilityRequest request =
                new CreateDoctorAvailabilityRequest();

        request.setDoctorId(doctorId);
        request.setDayOfWeek((short) 1);
        request.setStartTime(LocalTime.of(9, 0));
        request.setEndTime(LocalTime.of(13, 0));
        request.setSlotDurationMinutes(30);

        mockMvc.perform(
                        post("/api/v1/doctors/{doctorId}/availability",
                                doctorId)
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$.tenantId")
                        .value(tenantId.toString()))
                .andExpect(jsonPath("$.doctorId")
                        .value(doctorId.toString()))
                .andExpect(jsonPath("$.dayOfWeek")
                        .value(1))
                .andExpect(jsonPath("$.startTime")
                        .value("09:00:00"))
                .andExpect(jsonPath("$.endTime")
                        .value("13:00:00"))
                .andExpect(jsonPath("$.slotDurationMinutes")
                        .value(30))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        verify(doctorAvailabilityService)
                .createAvailability(
                        eq(tenantId),
                        any(CreateDoctorAvailabilityRequest.class)
                );
    }

    @Test
    void getAvailability_shouldReturn200() throws Exception {

        DoctorAvailabilityResponse response =
                createResponse();

        when(doctorAvailabilityService.getAvailability(
                tenantId,
                doctorId
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/doctors/{doctorId}/availability",
                                doctorId)
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].doctorId")
                        .value(doctorId.toString()))
                .andExpect(jsonPath("$[0].dayOfWeek")
                        .value(1));

        verify(doctorAvailabilityService)
                .getAvailability(tenantId, doctorId);
    }

    @Test
    void getAvailabilityById_shouldReturn200() throws Exception {

        DoctorAvailabilityResponse response =
                createResponse();

        when(doctorAvailabilityService.getAvailabilityById(
                tenantId,
                availabilityId
        )).thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/doctors/availability/{availabilityId}",
                                availabilityId)
                                .with(authentication(testAuthentication()))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$.doctorId")
                        .value(doctorId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("ACTIVE"));

        verify(doctorAvailabilityService)
                .getAvailabilityById(tenantId, availabilityId);
    }

    @Test
    void updateAvailability_shouldReturn200() throws Exception {

        DoctorAvailabilityResponse response =
                createResponse();

        when(doctorAvailabilityService.updateAvailability(
                eq(tenantId),
                eq(availabilityId),
                any(UpdateDoctorAvailabilityRequest.class)
        )).thenReturn(response);

        UpdateDoctorAvailabilityRequest request =
                new UpdateDoctorAvailabilityRequest();

        request.setDayOfWeek((short) 2);
        request.setStartTime(LocalTime.of(10, 0));
        request.setEndTime(LocalTime.of(14, 0));
        request.setSlotDurationMinutes(30);

        mockMvc.perform(
                        put("/api/v1/doctors/availability/{availabilityId}",
                                availabilityId)
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$.doctorId")
                        .value(doctorId.toString()));

        verify(doctorAvailabilityService)
                .updateAvailability(
                        eq(tenantId),
                        eq(availabilityId),
                        any(UpdateDoctorAvailabilityRequest.class)
                );
    }

    @Test
    void updateAvailabilityStatus_shouldReturn200() throws Exception {

        DoctorAvailabilityResponse response =
                createResponse();

        response.setStatus("INACTIVE");

        when(doctorAvailabilityService.updateAvailabilityStatus(
                eq(tenantId),
                eq(availabilityId),
                any(UpdateDoctorAvailabilityStatusRequest.class)
        )).thenReturn(response);

        UpdateDoctorAvailabilityStatusRequest request =
                new UpdateDoctorAvailabilityStatusRequest();

        request.setStatus("INACTIVE");

        mockMvc.perform(
                        patch(
                                "/api/v1/doctors/availability/{availabilityId}/status",
                                availabilityId
                        )
                                .with(authentication(testAuthentication()))
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id")
                        .value(availabilityId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("INACTIVE"));

        verify(doctorAvailabilityService)
                .updateAvailabilityStatus(
                        eq(tenantId),
                        eq(availabilityId),
                        any(UpdateDoctorAvailabilityStatusRequest.class)
                );
    }

    @Test
    void unauthenticatedRequest_shouldReturn401() throws Exception {

        mockMvc.perform(
                        get("/api/v1/doctors/{doctorId}/availability",
                                doctorId)
                )
                .andExpect(status().isUnauthorized());
    }

    private DoctorAvailabilityResponse createResponse() {

        DoctorAvailabilityResponse response =
                new DoctorAvailabilityResponse();

        response.setId(availabilityId);
        response.setTenantId(tenantId);
        response.setDoctorId(doctorId);
        response.setDayOfWeek((short) 1);
        response.setStartTime(LocalTime.of(9, 0));
        response.setEndTime(LocalTime.of(13, 0));
        response.setSlotDurationMinutes(30);
        response.setStatus("ACTIVE");

        return response;
    }
}