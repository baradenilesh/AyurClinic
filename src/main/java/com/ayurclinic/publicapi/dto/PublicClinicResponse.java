package com.ayurclinic.publicapi.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PublicClinicResponse {

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

    private String logoUrl;
}