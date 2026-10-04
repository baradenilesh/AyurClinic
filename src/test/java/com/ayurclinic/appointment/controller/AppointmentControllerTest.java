package com.ayurclinic.appointment.controller;

import com.ayurclinic.appointment.controller.AppointmentController;
import com.ayurclinic.appointment.dto.AppointmentCreateRequest;
import com.ayurclinic.appointment.dto.AppointmentResponse;
import com.ayurclinic.appointment.dto.AppointmentSummaryResponse;
import com.ayurclinic.appointment.enums.AppointmentStatus;
import com.ayurclinic.appointment.service.AppointmentService;
import com.ayurclinic.auth.security.CustomUserPrincipal;

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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AppointmentControllerTest {

    @Mock
    private AppointmentService appointmentService;

    @InjectMocks
    private AppointmentController appointmentController;

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    private UUID tenantId;
    private UUID userId;
    private UUID appointmentId;

    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        principal = new CustomUserPrincipal(
                userId,
                tenantId,
                "doctor@ayurclinic.com",
                "password",
                List.of(
                        new SimpleGrantedAuthority("ROLE_DOCTOR")
                ),
                true
        );

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        principal,
                        null,
                        principal.getAuthorities()
                );

        SecurityContext securityContext =
                SecurityContextHolder.createEmptyContext();

        securityContext.setAuthentication(authentication);

        SecurityContextHolder.setContext(securityContext);

        mockMvc = MockMvcBuilders
                .standaloneSetup(appointmentController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldCreateAppointment() throws Exception {

        AppointmentCreateRequest request =
                new AppointmentCreateRequest();

        request.setClinicId(UUID.randomUUID());
        request.setDoctorId(UUID.randomUUID());
        request.setPatientId(UUID.randomUUID());
        request.setAppointmentDate(
                LocalDate.of(2026, 9, 20)
        );
        request.setStartTime(
                LocalTime.of(10, 0)
        );
        request.setEndTime(
                LocalTime.of(10, 30)
        );
        request.setReason(
                "General consultation"
        );
        request.setNotes(
                "Test appointment"
        );

        AppointmentResponse response =
                new AppointmentResponse();

        response.setId(appointmentId);
        response.setTenantId(tenantId);
        response.setAppointmentDate(
                request.getAppointmentDate()
        );
        response.setStartTime(
                request.getStartTime()
        );
        response.setEndTime(
                request.getEndTime()
        );
        response.setStatus(
                AppointmentStatus.REQUESTED
        );

        when(appointmentService.createAppointment(
                eq(tenantId),
                any(AppointmentCreateRequest.class)
        )).thenReturn(response);

        mockMvc.perform(
                        post("/api/v1/appointments")
                                .principal(
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id")
                        .value(appointmentId.toString()))
                .andExpect(jsonPath("$.status")
                        .value("REQUESTED"));

        verify(appointmentService)
                .createAppointment(
                        eq(tenantId),
                        any(AppointmentCreateRequest.class)
                );
    }

    @Test
    void shouldGetDailyAppointments() throws Exception {

        LocalDate date =
                LocalDate.of(2026, 9, 20);

        AppointmentResponse response =
                new AppointmentResponse();

        response.setId(appointmentId);
        response.setTenantId(tenantId);
        response.setAppointmentDate(date);
        response.setStartTime(
                LocalTime.of(10, 0)
        );
        response.setEndTime(
                LocalTime.of(10, 30)
        );
        response.setStatus(
                AppointmentStatus.CONFIRMED
        );

        when(appointmentService.getDailyAppointments(
                tenantId,
                date
        )).thenReturn(List.of(response));

        mockMvc.perform(
                        get("/api/v1/appointments/daily")
                                .principal(
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                                .param(
                                        "date",
                                        "2026-09-20"
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()")
                        .value(1))
                .andExpect(jsonPath("$[0].status")
                        .value("CONFIRMED"));

        verify(appointmentService)
                .getDailyAppointments(
                        tenantId,
                        date
                );
    }

    @Test
    void shouldGetDailySummary() throws Exception {

        LocalDate date = LocalDate.of(2026, 9, 20);

        AppointmentSummaryResponse summary =
                new AppointmentSummaryResponse();

        summary.setDate(date);
        summary.setTotal(5);
        summary.setRequested(1);
        summary.setConfirmed(1);
        summary.setCompleted(1);
        summary.setCancelled(1);
        summary.setNoShow(1);

        when(appointmentService.getDailySummary(
                tenantId,
                date
        )).thenReturn(summary);

        mockMvc.perform(
                        get("/api/v1/appointments/summary")
                                .param("date", "2026-09-20")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.date[0]").value(2026))
                .andExpect(jsonPath("$.date[1]").value(9))
                .andExpect(jsonPath("$.date[2]").value(20))
                .andExpect(jsonPath("$.total").value(5))
                .andExpect(jsonPath("$.requested").value(1))
                .andExpect(jsonPath("$.confirmed").value(1))
                .andExpect(jsonPath("$.completed").value(1))
                .andExpect(jsonPath("$.cancelled").value(1))
                .andExpect(jsonPath("$.noShow").value(1));

        verify(appointmentService).getDailySummary(
                tenantId,
                date
        );
    }
    @Test
    void shouldConfirmAppointment() throws Exception {

        AppointmentResponse response =
                new AppointmentResponse();

        response.setId(appointmentId);
        response.setTenantId(tenantId);
        response.setStatus(
                AppointmentStatus.CONFIRMED
        );

        when(appointmentService.confirmAppointment(
                tenantId,
                appointmentId
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/appointments/"
                                        + appointmentId
                                        + "/confirm"
                        )
                                .principal(
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CONFIRMED"));

        verify(appointmentService)
                .confirmAppointment(
                        tenantId,
                        appointmentId
                );
    }

    @Test
    void shouldCancelAppointment() throws Exception {

        AppointmentResponse response =
                new AppointmentResponse();

        response.setId(appointmentId);
        response.setTenantId(tenantId);
        response.setStatus(
                AppointmentStatus.CANCELLED
        );

        when(appointmentService.cancelAppointment(
                tenantId,
                appointmentId
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/appointments/"
                                        + appointmentId
                                        + "/cancel"
                        )
                                .principal(
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("CANCELLED"));
    }

    @Test
    void shouldCompleteAppointment() throws Exception {

        AppointmentResponse response =
                new AppointmentResponse();

        response.setId(appointmentId);
        response.setTenantId(tenantId);
        response.setStatus(
                AppointmentStatus.COMPLETED
        );

        when(appointmentService.completeAppointment(
                tenantId,
                appointmentId
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/appointments/"
                                        + appointmentId
                                        + "/complete"
                        )
                                .principal(
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("COMPLETED"));
    }

    @Test
    void shouldMarkNoShow() throws Exception {

        AppointmentResponse response =
                new AppointmentResponse();

        response.setId(appointmentId);
        response.setTenantId(tenantId);
        response.setStatus(
                AppointmentStatus.NO_SHOW
        );

        when(appointmentService.markNoShow(
                tenantId,
                appointmentId
        )).thenReturn(response);

        mockMvc.perform(
                        patch(
                                "/api/v1/appointments/"
                                        + appointmentId
                                        + "/no-show"
                        )
                                .principal(
                                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("NO_SHOW"));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}