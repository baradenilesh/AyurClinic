package com.ayurclinic.dashboard.service;

import com.ayurclinic.appointment.enums.AppointmentStatus;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.dashboard.dto.DashboardSummaryResponse;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.ayurclinic.followup.repository.FollowUpRepository;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.billing.enums.PaymentStatus;
import com.ayurclinic.billing.repository.PaymentRepository;
import com.ayurclinic.billing.repository.InvoiceRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final FollowUpRepository followUpRepository;
    private final ConsultationRepository consultationRepository;
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;

    public DashboardService(
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository, FollowUpRepository followUpRepository, ConsultationRepository consultationRepository, PaymentRepository paymentRepository, InvoiceRepository invoiceRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.followUpRepository = followUpRepository;
        this.consultationRepository = consultationRepository;
        this.paymentRepository = paymentRepository;
        this.invoiceRepository = invoiceRepository;
    }

    public DashboardSummaryResponse getSummary(UUID tenantId) {

        LocalDate today = LocalDate.now();

        long totalDoctors =
                doctorRepository.countByTenantId(tenantId);

        long activeDoctors =
                doctorRepository.countByTenantIdAndStatus(
                        tenantId,
                        "ACTIVE"
                );

        long totalPatients =
                patientRepository.countByTenantId(tenantId);

        long todayAppointments =
                appointmentRepository.countByTenantIdAndAppointmentDate(
                        tenantId,
                        today
                );

        long todayRequestedAppointments =
                appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                        tenantId,
                        today,
                        AppointmentStatus.REQUESTED
                );

        long todayConfirmedAppointments =
                appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                        tenantId,
                        today,
                        AppointmentStatus.CONFIRMED
                );

        long todayCompletedAppointments =
                appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                        tenantId,
                        today,
                        AppointmentStatus.COMPLETED
                );

        long todayCancelledAppointments =
                appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                        tenantId,
                        today,
                        AppointmentStatus.CANCELLED
                );

        long todayNoShowAppointments =
                appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                        tenantId,
                        today,
                        AppointmentStatus.NO_SHOW
                );

        long todayFollowUps =
                followUpRepository.countByTenantIdAndFollowUpDate(
                        tenantId,
                        today
                );

        long todayScheduledFollowUps =
                followUpRepository.countByTenantIdAndFollowUpDateAndStatus(
                        tenantId,
                        today,
                        "SCHEDULED"
                );

        long todayCompletedFollowUps =
                followUpRepository.countByTenantIdAndFollowUpDateAndStatus(
                        tenantId,
                        today,
                        "COMPLETED"
                );
        long todayCompletedConsultations =
                consultationRepository.countByTenantIdAndConsultationDateAndStatus(
                        tenantId,
                        today,
                        "COMPLETED"
                );
        BigDecimal todayRevenue =
                paymentRepository.sumAmountByTenantIdAndPaymentDateAndStatus(
                        tenantId,
                        today,
                        PaymentStatus.COMPLETED
                );
        BigDecimal outstandingAmount =
                invoiceRepository.sumOutstandingAmountByTenantId(
                        tenantId
                );

        return new DashboardSummaryResponse(
                totalDoctors,
                activeDoctors,
                totalPatients,
                todayAppointments,
                todayRequestedAppointments,
                todayConfirmedAppointments,
                todayCompletedAppointments,
                todayCancelledAppointments,
                todayNoShowAppointments,
                todayFollowUps,
                todayScheduledFollowUps,
                todayCompletedFollowUps,
                todayCompletedConsultations,
                todayRevenue,
                outstandingAmount
        );
    }
}