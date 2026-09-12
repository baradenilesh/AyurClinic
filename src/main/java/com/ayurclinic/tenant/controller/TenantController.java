package com.ayurclinic.tenant.controller;

import com.ayurclinic.common.api.ApiResponse;
import com.ayurclinic.tenant.dto.CreateTenantRequest;
import com.ayurclinic.tenant.dto.TenantResponse;
import com.ayurclinic.tenant.service.TenantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/tenants")
public class TenantController {
    private final TenantService service;

    public TenantController(TenantService service) {
        this.service = service;
    }

    @PostMapping
    public ApiResponse<TenantResponse> create(@Valid @RequestBody CreateTenantRequest request) {
        return ApiResponse.success(service.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<TenantResponse> get(@PathVariable UUID id) {
        return ApiResponse.success(service.get(id));
    }
}
