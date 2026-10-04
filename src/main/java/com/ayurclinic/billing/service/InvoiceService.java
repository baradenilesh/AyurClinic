package com.ayurclinic.billing.service;

import com.ayurclinic.appointment.entity.Appointment;
import com.ayurclinic.appointment.repository.AppointmentRepository;
import com.ayurclinic.billing.dto.InvoiceCreateRequest;
import com.ayurclinic.billing.dto.InvoiceResponse;
import com.ayurclinic.billing.dto.InvoiceUpdateRequest;
import com.ayurclinic.billing.entity.Invoice;
import com.ayurclinic.billing.enums.InvoiceStatus;
import com.ayurclinic.billing.repository.InvoiceRepository;
import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.consultation.entity.Consultation;
import com.ayurclinic.consultation.repository.ConsultationRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ClinicRepository clinicRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;

    public InvoiceService(
            InvoiceRepository invoiceRepository,
            ClinicRepository clinicRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            ConsultationRepository consultationRepository
    ) {
        this.invoiceRepository = invoiceRepository;
        this.clinicRepository = clinicRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
    }

    public InvoiceResponse createInvoice(
            UUID tenantId,
            InvoiceCreateRequest request
    ) {
        validateClinicAndPatient(
                tenantId,
                request.getClinicId(),
                request.getPatientId()
        );

        validateAppointment(
                tenantId,
                request.getAppointmentId(),
                request.getClinicId(),
                request.getPatientId()
        );

        validateConsultation(
                tenantId,
                request.getConsultationId(),
                request.getClinicId(),
                request.getPatientId()
        );

        BigDecimal subtotal = defaultAmount(request.getSubtotal());
        BigDecimal discount = defaultAmount(request.getDiscount());
        BigDecimal tax = defaultAmount(request.getTax());

        validateAmounts(subtotal, discount, tax);

        BigDecimal totalAmount = calculateTotal(
                subtotal,
                discount,
                tax
        );

        Invoice invoice = new Invoice();

        invoice.setTenantId(tenantId);
        invoice.setClinicId(request.getClinicId());
        invoice.setPatientId(request.getPatientId());
        invoice.setAppointmentId(request.getAppointmentId());
        invoice.setConsultationId(request.getConsultationId());

        invoice.setInvoiceNumber(generateInvoiceNumber(tenantId));

        invoice.setInvoiceDate(
                request.getInvoiceDate() != null
                        ? request.getInvoiceDate()
                        : LocalDate.now()
        );

        invoice.setDueDate(request.getDueDate());

        invoice.setSubtotal(subtotal);
        invoice.setDiscount(discount);
        invoice.setTax(tax);
        invoice.setTotalAmount(totalAmount);
        invoice.setPaidAmount(BigDecimal.ZERO);

        invoice.setStatus(InvoiceStatus.DRAFT);
        invoice.setNotes(request.getNotes());

        invoice.setCreatedAt(OffsetDateTime.now());
        invoice.setUpdatedAt(OffsetDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);

        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public InvoiceResponse getInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {
        Invoice invoice = getInvoiceEntity(
                tenantId,
                invoiceId
        );

        return toResponse(invoice);
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAllInvoices(
            UUID tenantId
    ) {
        return invoiceRepository
                .findByTenantIdOrderByInvoiceDateDesc(tenantId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getPatientInvoices(
            UUID tenantId,
            UUID patientId
    ) {
        patientRepository
                .findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Patient not found"
                        )
                );

        return invoiceRepository
                .findByTenantIdAndPatientIdOrderByInvoiceDateDesc(
                        tenantId,
                        patientId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getAppointmentInvoices(
            UUID tenantId,
            UUID appointmentId
    ) {
        validateAppointmentExists(
                tenantId,
                appointmentId
        );

        return invoiceRepository
                .findByTenantIdAndAppointmentIdOrderByInvoiceDateDesc(
                        tenantId,
                        appointmentId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getConsultationInvoices(
            UUID tenantId,
            UUID consultationId
    ) {
        consultationRepository
                .findByIdAndTenantId(
                        consultationId,
                        tenantId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Consultation not found"
                        )
                );

        return invoiceRepository
                .findByTenantIdAndConsultationIdOrderByInvoiceDateDesc(
                        tenantId,
                        consultationId
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InvoiceResponse> getInvoicesByStatus(
            UUID tenantId,
            InvoiceStatus status
    ) {
        return invoiceRepository
                .findByTenantIdAndStatusOrderByInvoiceDateDesc(
                        tenantId,
                        status
                )
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public InvoiceResponse updateInvoice(
            UUID tenantId,
            UUID invoiceId,
            InvoiceUpdateRequest request
    ) {
        Invoice invoice = getInvoiceEntity(
                tenantId,
                invoiceId
        );

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft invoices can be updated"
            );
        }

        UUID clinicId = invoice.getClinicId();
        UUID patientId = invoice.getPatientId();

        if (request.getAppointmentId() != null) {
            validateAppointment(
                    tenantId,
                    request.getAppointmentId(),
                    clinicId,
                    patientId
            );

            invoice.setAppointmentId(
                    request.getAppointmentId()
            );
        }

        if (request.getConsultationId() != null) {
            validateConsultation(
                    tenantId,
                    request.getConsultationId(),
                    clinicId,
                    patientId
            );

            invoice.setConsultationId(
                    request.getConsultationId()
            );
        }

        if (request.getDueDate() != null) {
            invoice.setDueDate(request.getDueDate());
        }

        if (request.getSubtotal() != null) {
            invoice.setSubtotal(request.getSubtotal());
        }

        if (request.getDiscount() != null) {
            invoice.setDiscount(request.getDiscount());
        }

        if (request.getTax() != null) {
            invoice.setTax(request.getTax());
        }

        if (request.getNotes() != null) {
            invoice.setNotes(request.getNotes());
        }

        BigDecimal subtotal = defaultAmount(
                invoice.getSubtotal()
        );

        BigDecimal discount = defaultAmount(
                invoice.getDiscount()
        );

        BigDecimal tax = defaultAmount(
                invoice.getTax()
        );

        validateAmounts(
                subtotal,
                discount,
                tax
        );

        invoice.setSubtotal(subtotal);
        invoice.setDiscount(discount);
        invoice.setTax(tax);

        invoice.setTotalAmount(
                calculateTotal(
                        subtotal,
                        discount,
                        tax
                )
        );

        invoice.setUpdatedAt(OffsetDateTime.now());

        Invoice saved = invoiceRepository.save(invoice);

        return toResponse(saved);
    }

    public InvoiceResponse issueInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {
        Invoice invoice = getInvoiceEntity(
                tenantId,
                invoiceId
        );

        if (invoice.getStatus() != InvoiceStatus.DRAFT) {
            throw new IllegalStateException(
                    "Only draft invoices can be issued"
            );
        }

        if (invoice.getTotalAmount() == null
                || invoice.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalStateException(
                    "Invoice total amount must be greater than zero"
            );
        }

        invoice.setStatus(InvoiceStatus.ISSUED);
        invoice.setUpdatedAt(OffsetDateTime.now());

        return toResponse(
                invoiceRepository.save(invoice)
        );
    }

    public InvoiceResponse cancelInvoice(
            UUID tenantId,
            UUID invoiceId
    ) {
        Invoice invoice = getInvoiceEntity(
                tenantId,
                invoiceId
        );

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new IllegalStateException(
                    "Paid invoices cannot be cancelled"
            );
        }

        if (invoice.getStatus() == InvoiceStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Invoice is already cancelled"
            );
        }

        invoice.setStatus(InvoiceStatus.CANCELLED);
        invoice.setUpdatedAt(OffsetDateTime.now());

        return toResponse(
                invoiceRepository.save(invoice)
        );
    }

    private Invoice getInvoiceEntity(
            UUID tenantId,
            UUID invoiceId
    ) {
        return invoiceRepository
                .findByIdAndTenantId(invoiceId, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Invoice not found"
                        )
                );
    }

    private void validateClinicAndPatient(
            UUID tenantId,
            UUID clinicId,
            UUID patientId
    ) {
        Clinic clinic = clinicRepository
                .findByIdAndTenantId(clinicId, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Clinic not found"
                        )
                );

        Patient patient = patientRepository
                .findByIdAndTenantId(patientId, tenantId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Patient not found"
                        )
                );

        /*
         * The current Patient entity/repository does not expose
         * a clinicId lookup, so tenant ownership is validated here.
         *
         * The clinic variable is intentionally retrieved to guarantee
         * that the supplied clinic belongs to the current tenant.
         */
        if (clinic.getId() == null || patient.getId() == null) {
            throw new IllegalArgumentException(
                    "Invalid clinic or patient"
            );
        }
    }

    private void validateAppointment(
            UUID tenantId,
            UUID appointmentId,
            UUID clinicId,
            UUID patientId
    ) {
        if (appointmentId == null) {
            return;
        }

        Appointment appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Appointment not found"
                        )
                );

        if (!tenantId.equals(appointment.getTenantId())) {
            throw new IllegalArgumentException(
                    "Appointment does not belong to the current tenant"
            );
        }

        if (!clinicId.equals(appointment.getClinicId())) {
            throw new IllegalArgumentException(
                    "Appointment does not belong to the supplied clinic"
            );
        }

        if (!patientId.equals(appointment.getPatientId())) {
            throw new IllegalArgumentException(
                    "Appointment does not belong to the supplied patient"
            );
        }
    }

    private void validateConsultation(
            UUID tenantId,
            UUID consultationId,
            UUID clinicId,
            UUID patientId
    ) {
        if (consultationId == null) {
            return;
        }

        Consultation consultation = consultationRepository
                .findByIdAndTenantId(
                        consultationId,
                        tenantId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Consultation not found"
                        )
                );

        if (!clinicId.equals(consultation.getClinicId())) {
            throw new IllegalArgumentException(
                    "Consultation does not belong to the supplied clinic"
            );
        }

        if (!patientId.equals(consultation.getPatientId())) {
            throw new IllegalArgumentException(
                    "Consultation does not belong to the supplied patient"
            );
        }
    }

    private void validateAppointmentExists(
            UUID tenantId,
            UUID appointmentId
    ) {
        Appointment appointment = appointmentRepository
                .findById(appointmentId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Appointment not found"
                        )
                );

        if (!tenantId.equals(appointment.getTenantId())) {
            throw new IllegalArgumentException(
                    "Appointment does not belong to the current tenant"
            );
        }
    }

    private BigDecimal calculateTotal(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax
    ) {
        return subtotal
                .subtract(discount)
                .add(tax);
    }

    private void validateAmounts(
            BigDecimal subtotal,
            BigDecimal discount,
            BigDecimal tax
    ) {
        if (subtotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Subtotal cannot be negative"
            );
        }

        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Discount cannot be negative"
            );
        }

        if (tax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    "Tax cannot be negative"
            );
        }

        if (discount.compareTo(subtotal) > 0) {
            throw new IllegalArgumentException(
                    "Discount cannot exceed subtotal"
            );
        }
    }

    private BigDecimal defaultAmount(
            BigDecimal amount
    ) {
        return amount == null
                ? BigDecimal.ZERO
                : amount;
    }

    private String generateInvoiceNumber(
            UUID tenantId
    ) {
        String invoiceNumber;

        do {
            invoiceNumber =
                    "INV-"
                            + LocalDate.now()
                            .toString()
                            .replace("-", "")
                            + "-"
                            + UUID.randomUUID()
                            .toString()
                            .substring(0, 8)
                            .toUpperCase();
        } while (
                invoiceRepository
                        .findByTenantIdAndInvoiceNumber(
                                tenantId,
                                invoiceNumber
                        )
                        .isPresent()
        );

        return invoiceNumber;
    }

    private InvoiceResponse toResponse(
            Invoice invoice
    ) {
        BigDecimal totalAmount =
                defaultAmount(invoice.getTotalAmount());

        BigDecimal paidAmount =
                defaultAmount(invoice.getPaidAmount());

        BigDecimal outstandingAmount =
                totalAmount.subtract(paidAmount);

        if (outstandingAmount.compareTo(BigDecimal.ZERO) < 0) {
            outstandingAmount = BigDecimal.ZERO;
        }

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .tenantId(invoice.getTenantId())
                .clinicId(invoice.getClinicId())
                .patientId(invoice.getPatientId())
                .appointmentId(invoice.getAppointmentId())
                .consultationId(invoice.getConsultationId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .invoiceDate(invoice.getInvoiceDate())
                .dueDate(invoice.getDueDate())
                .subtotal(invoice.getSubtotal())
                .discount(invoice.getDiscount())
                .tax(invoice.getTax())
                .totalAmount(totalAmount)
                .paidAmount(paidAmount)
                .outstandingAmount(outstandingAmount)
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }
}