package com.ayurclinic.clinic.dto;

import com.ayurclinic.clinic.entity.ClinicStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Builder
public class ClinicResponse {

    private UUID id;
    private UUID tenantId;
    private String name;

    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String postalCode;
    private String country;

    private String phone;
    private String email;
    private String website;
    private String logoS3Key;

    private ClinicStatus status;

    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}