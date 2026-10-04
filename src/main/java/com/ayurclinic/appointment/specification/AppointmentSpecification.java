package com.ayurclinic.appointment.specification;

import com.ayurclinic.appointment.dto.AppointmentSearchRequest;
import com.ayurclinic.appointment.entity.Appointment;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class AppointmentSpecification {

    private AppointmentSpecification() {
    }

    public static Specification<Appointment> search(
            UUID tenantId,
            AppointmentSearchRequest request) {

        return (root, query, criteriaBuilder) -> {

            var predicates = criteriaBuilder.conjunction();

            // Mandatory tenant isolation
            predicates = criteriaBuilder.and(
                    predicates,
                    criteriaBuilder.equal(
                            root.get("tenantId"),
                            tenantId
                    )
            );

            if (request.getDate() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("appointmentDate"),
                                request.getDate()
                        )
                );
            }

            if (request.getFromDate() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.greaterThanOrEqualTo(
                                root.get("appointmentDate"),
                                request.getFromDate()
                        )
                );
            }

            if (request.getToDate() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.lessThanOrEqualTo(
                                root.get("appointmentDate"),
                                request.getToDate()
                        )
                );
            }

            if (request.getDoctorId() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("doctorId"),
                                request.getDoctorId()
                        )
                );
            }

            if (request.getPatientId() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("patientId"),
                                request.getPatientId()
                        )
                );
            }

            if (request.getClinicId() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("clinicId"),
                                request.getClinicId()
                        )
                );
            }

            if (request.getStatus() != null) {
                predicates = criteriaBuilder.and(
                        predicates,
                        criteriaBuilder.equal(
                                root.get("status"),
                                request.getStatus()
                        )
                );
            }

            return predicates;
        };
    }
}