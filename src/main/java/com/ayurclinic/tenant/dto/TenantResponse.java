package com.ayurclinic.tenant.dto;

import com.ayurclinic.tenant.entity.Tenant;
import java.time.Instant;
import java.util.UUID;

public record TenantResponse(
        UUID id,
        String name,
        String status,
        Instant createdAt
) {
    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getStatus().name(),
                tenant.getCreatedAt()
        );
    }
}
