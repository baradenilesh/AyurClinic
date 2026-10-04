package com.ayurclinic.clinic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ClinicCreateRequest {

    @NotBlank
    @Size(max = 200)
    private String name;

    @Size(max = 255)
    private String addressLine1;

    @Size(max = 255)
    private String addressLine2;

    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String state;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 100)
    private String country;

    @Size(max = 30)
    private String phone;

    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 255)
    private String website;

    @Size(max = 500)
    private String logoS3Key;
}