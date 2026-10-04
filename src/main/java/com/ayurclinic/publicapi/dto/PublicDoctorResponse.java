package com.ayurclinic.publicapi.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PublicDoctorResponse {

    private UUID id;

    private String name;

    private String qualification;

    private String specialization;

    private Integer experienceYears;

    private String bio;

    private String photoUrl;
}