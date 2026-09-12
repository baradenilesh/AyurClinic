package com.ayurclinic.tenant.service;

import com.ayurclinic.common.exception.ResourceNotFoundException;
import com.ayurclinic.tenant.dto.CreateTenantRequest;
import com.ayurclinic.tenant.dto.TenantResponse;
import com.ayurclinic.tenant.entity.Tenant;
import com.ayurclinic.tenant.repository.TenantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TenantService {
    private final TenantRepository repository;

    public TenantService(TenantRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TenantResponse create(CreateTenantRequest request) {
        if (repository.existsByNameIgnoreCase(request.name())) {
            throw new IllegalArgumentException("Tenant name already exists");
        }
        return TenantResponse.from(repository.save(new Tenant(request.name())));
    }

    @Transactional(readOnly = true)
    public TenantResponse get(UUID id) {
        Tenant tenant = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tenant not found: " + id));
        return TenantResponse.from(tenant);
    }
}
