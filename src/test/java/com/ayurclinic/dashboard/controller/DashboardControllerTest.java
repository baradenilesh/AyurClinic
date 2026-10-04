package com.ayurclinic.dashboard.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.dashboard.dto.DashboardSummaryResponse;
import com.ayurclinic.dashboard.service.DashboardService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class DashboardControllerTest {

    @Mock
    private DashboardService dashboardService;

    @InjectMocks
    private DashboardController dashboardController;

    private MockMvc mockMvc;

    private UUID tenantId;
    private UUID userId;

    private CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {

        tenantId = UUID.randomUUID();
        userId = UUID.randomUUID();

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
                .standaloneSetup(dashboardController)
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver()
                )
                .build();
    }

    @Test
    void shouldGetDashboardSummary() throws Exception {

        DashboardSummaryResponse response =
                new DashboardSummaryResponse(
                        5L,
                        4L,
                        100L,
                        10L,
                        1L,
                        4L,
                        3L,
                        1L,
                        1L,
                        3L,
                        2L,
                        1L,
                        3L,
                        new BigDecimal("4500.00"),
                        new BigDecimal("12500.00")
                );

        when(dashboardService.getSummary(tenantId))
                .thenReturn(response);

        mockMvc.perform(
                        get("/api/v1/dashboard/summary")
                                .principal(
                                        new UsernamePasswordAuthenticationToken(
                                                principal,
                                                null,
                                                principal.getAuthorities()
                                        )
                                )
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalDoctors").value(5))
                .andExpect(jsonPath("$.activeDoctors").value(4))
                .andExpect(jsonPath("$.totalPatients").value(100))
                .andExpect(jsonPath("$.todayAppointments").value(10))
                .andExpect(jsonPath("$.todayRequestedAppointments").value(1))
                .andExpect(jsonPath("$.todayConfirmedAppointments").value(4))
                .andExpect(jsonPath("$.todayCompletedAppointments").value(3))
                .andExpect(jsonPath("$.todayCancelledAppointments").value(1))
                .andExpect(jsonPath("$.todayNoShowAppointments").value(1))
                .andExpect(jsonPath("$.todayFollowUps").value(3))
                .andExpect(jsonPath("$.todayScheduledFollowUps").value(2))
                .andExpect(jsonPath("$.todayCompletedFollowUps").value(1))
                .andExpect(jsonPath("$.todayCompletedConsultations").value(3))
                .andExpect(jsonPath("$.todayRevenue").value(4500.00))
                .andExpect(jsonPath("$.outstandingAmount").value(12500.00));

        verify(dashboardService)
                .getSummary(eq(tenantId));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
}