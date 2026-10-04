package com.ayurclinic.dashboard.controller;

import com.ayurclinic.auth.security.CustomUserPrincipal;
import com.ayurclinic.dashboard.dto.DashboardSummaryResponse;
import com.ayurclinic.dashboard.service.DashboardService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public DashboardSummaryResponse getSummary(
            @AuthenticationPrincipal CustomUserPrincipal principal
    ) {
        return dashboardService.getSummary(principal.getTenantId());
    }
}