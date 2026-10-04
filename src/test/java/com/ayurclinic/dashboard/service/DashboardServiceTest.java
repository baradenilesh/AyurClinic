package com.ayurclinic.dashboard.service;

import com.ayurclinic.appointment.enums.AppointmentStatus;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.billing.enums.PaymentStatus;
import com.ayurclinic.billing.repository.InvoiceRepository;
import com.ayurclinic.billing.repository.PaymentRepository;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.dashboard.dto.DashboardSummaryResponse;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.followup.repository.FollowUpRepository;
import com.ayurclinic.patient.repository.PatientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private FollowUpRepository followUpRepository;

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @InjectMocks
    private DashboardService dashboardService;

    @Test
    void shouldReturnDashboardSummary() {

        UUID tenantId = UUID.randomUUID();
        LocalDate today = LocalDate.now();

        when(doctorRepository.countByTenantId(tenantId))
                .thenReturn(5L);

        when(doctorRepository.countByTenantIdAndStatus(
                tenantId, "ACTIVE"))
                .thenReturn(4L);

        when(patientRepository.countByTenantId(tenantId))
                .thenReturn(100L);

        when(appointmentRepository.countByTenantIdAndAppointmentDate(
                tenantId, today))
                .thenReturn(10L);

        when(appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                tenantId, today, AppointmentStatus.REQUESTED))
                .thenReturn(1L);

        when(appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                tenantId, today, AppointmentStatus.CONFIRMED))
                .thenReturn(4L);

        when(appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                tenantId, today, AppointmentStatus.COMPLETED))
                .thenReturn(3L);

        when(appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                tenantId, today, AppointmentStatus.CANCELLED))
                .thenReturn(1L);

        when(appointmentRepository.countByTenantIdAndAppointmentDateAndStatus(
                tenantId, today, AppointmentStatus.NO_SHOW))
                .thenReturn(1L);

        when(followUpRepository.countByTenantIdAndFollowUpDate(
                tenantId, today))
                .thenReturn(3L);

        when(followUpRepository.countByTenantIdAndFollowUpDateAndStatus(
                tenantId, today, "SCHEDULED"))
                .thenReturn(2L);

        when(followUpRepository.countByTenantIdAndFollowUpDateAndStatus(
                tenantId, today, "COMPLETED"))
                .thenReturn(1L);

        when(consultationRepository
                .countByTenantIdAndConsultationDateAndStatus(
                        tenantId, today, "COMPLETED"))
                .thenReturn(3L);

        when(paymentRepository
                .sumAmountByTenantIdAndPaymentDateAndStatus(
                        tenantId, today, PaymentStatus.COMPLETED))
                .thenReturn(new BigDecimal("4500.00"));

        when(invoiceRepository
                .sumOutstandingAmountByTenantId(tenantId))
                .thenReturn(new BigDecimal("12500.00"));

        DashboardSummaryResponse response =
                dashboardService.getSummary(tenantId);
        verify(appointmentRepository)
                .countByTenantIdAndAppointmentDateAndStatus(
                        tenantId,
                        today,
                        AppointmentStatus.REQUESTED
                );
        assertThat(response).isNotNull();

        assertThat(response.getTotalDoctors())
                .isEqualTo(5L);

        assertThat(response.getActiveDoctors())
                .isEqualTo(4L);

        assertThat(response.getTotalPatients())
                .isEqualTo(100L);

        assertThat(response.getTodayAppointments())
                .isEqualTo(10L);

        assertThat(response.getTodayRequestedAppointments())
                .isEqualTo(1L);

        assertThat(response.getTodayConfirmedAppointments())
                .isEqualTo(4L);

        assertThat(response.getTodayCompletedAppointments())
                .isEqualTo(3L);

        assertThat(response.getTodayCancelledAppointments())
                .isEqualTo(1L);

        assertThat(response.getTodayNoShowAppointments())
                .isEqualTo(1L);

        assertThat(response.getTodayFollowUps())
                .isEqualTo(3L);

        assertThat(response.getTodayScheduledFollowUps())
                .isEqualTo(2L);

        assertThat(response.getTodayCompletedFollowUps())
                .isEqualTo(1L);

        assertThat(response.getTodayCompletedConsultations())
                .isEqualTo(3L);

        assertThat(response.getTodayRevenue())
                .isEqualByComparingTo("4500.00");

        assertThat(response.getOutstandingAmount())
                .isEqualByComparingTo("12500.00");
    }
}