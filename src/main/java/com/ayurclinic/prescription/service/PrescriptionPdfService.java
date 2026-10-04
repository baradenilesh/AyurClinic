package com.ayurclinic.prescription.service;

import com.ayurclinic.clinic.entity.Clinic;
import com.ayurclinic.clinic.repository.ClinicRepository;
import com.ayurclinic.doctor.entity.Doctor;
import com.ayurclinic.doctor.repository.DoctorRepository;
import com.ayurclinic.patient.entity.Patient;
import com.ayurclinic.patient.repository.PatientRepository;
import com.ayurclinic.prescription.entity.Prescription;
import com.ayurclinic.prescription.entity.PrescriptionItem;
import com.ayurclinic.prescription.repository.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.openpdf.text.Document;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfPageEventHelper;
import org.openpdf.text.pdf.PdfWriter;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PrescriptionPdfService {

    private final PrescriptionRepository prescriptionRepository;
    private final ClinicRepository clinicRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;

    public byte[] generatePrescriptionPdf(
            UUID tenantId,
            UUID prescriptionId
    ) {
        validateTenant(tenantId);

        Prescription prescription =
                prescriptionRepository
                        .findByIdAndTenantId(
                                prescriptionId,
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Prescription not found"
                                )
                        );

        Clinic clinic =
                clinicRepository
                        .findByIdAndTenantId(
                                prescription.getClinicId(),
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Clinic not found"
                                )
                        );

        Doctor doctor =
                doctorRepository
                        .findByIdAndTenantId(
                                prescription.getDoctorId(),
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Doctor not found"
                                )
                        );

        Patient patient =
                patientRepository
                        .findByIdAndTenantId(
                                prescription.getPatientId(),
                                tenantId
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Patient not found"
                                )
                        );
        if (prescription.getItems() == null ||
                prescription.getItems().isEmpty()) {

            throw new IllegalStateException(
                    "Prescription has no medicines"
            );
        }
        try {
            return buildPdf(
                    prescription,
                    clinic,
                    doctor,
                    patient
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Failed to generate prescription PDF",
                    exception
            );
        }
    }

    private static class PrescriptionPdfFooter
            extends PdfPageEventHelper {

        private final Font footerFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        7,
                        Font.NORMAL
                );

        @Override
        public void onEndPage(
                PdfWriter writer,
                Document document
        ) {
            String generatedAt =
                    OffsetDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "dd-MM-yyyy HH:mm"
                                    )
                            );

            PdfPTable footerTable =
                    new PdfPTable(2);

            try {
                footerTable.setWidths(
                        new float[]{1f, 1f}
                );

                footerTable.setTotalWidth(
                        document.right() -
                                document.left()
                );

                footerTable.getDefaultCell()
                        .setBorder(
                                PdfPCell.NO_BORDER
                        );

                footerTable.getDefaultCell()
                        .setPadding(0);

                footerTable.addCell(
                        new Phrase(
                                "Generated by AyurClinic",
                                footerFont
                        )
                );

                PdfPCell rightCell =
                        new PdfPCell(
                                new Phrase(
                                        "Generated: " +
                                                generatedAt +
                                                "    Page " +
                                                writer.getPageNumber(),
                                        footerFont
                                )
                        );

                rightCell.setBorder(
                        PdfPCell.NO_BORDER
                );

                rightCell.setHorizontalAlignment(
                        Element.ALIGN_RIGHT
                );

                footerTable.addCell(rightCell);

                footerTable.writeSelectedRows(
                        0,
                        -1,
                        document.left(),
                        document.bottom() - 10,
                        writer.getDirectContent()
                );

            } catch (Exception exception) {
                throw new IllegalStateException(
                        "Failed to render PDF footer",
                        exception
                );
            }
        }
    }

    private byte[] buildPdf(
            Prescription prescription,
            Clinic clinic,
            Doctor doctor,
            Patient patient
    ) throws Exception {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        Document document =
                new Document(
                        PageSize.A4,
                        36,
                        36,
                        36,
                        36
                );

        PdfWriter writer =
                PdfWriter.getInstance(
                        document,
                        outputStream
                );

        writer.setPageEvent(
                new PrescriptionPdfFooter()
        );

        document.open();

        Font clinicFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        18,
                        Font.BOLD
                );

        Font titleFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        15,
                        Font.BOLD
                );

        Font sectionFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        11,
                        Font.BOLD
                );

        Font normalFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        9,
                        Font.NORMAL
                );

        Font smallFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA,
                        8,
                        Font.NORMAL
                );

        Font tableHeaderFont =
                FontFactory.getFont(
                        FontFactory.HELVETICA_BOLD,
                        7,
                        Font.BOLD
                );

        // ---------------------------------------------------------
        // Clinic Header
        // ---------------------------------------------------------

        Paragraph clinicName =
                new Paragraph(
                        safe(clinic.getName()),
                        clinicFont
                );

        clinicName.setAlignment(Element.ALIGN_CENTER);
        document.add(clinicName);

        String clinicContact =
                joinNonEmpty(
                        clinic.getAddressLine1(),
                        clinic.getAddressLine2(),
                        clinic.getCity(),
                        clinic.getState(),
                        clinic.getPostalCode()
                );

        if (!clinicContact.isBlank()) {
            Paragraph address =
                    new Paragraph(
                            clinicContact,
                            smallFont
                    );

            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
        }

        String contactDetails =
                joinNonEmpty(
                        clinic.getPhone(),
                        clinic.getEmail(),
                        clinic.getWebsite()
                );

        if (!contactDetails.isBlank()) {
            Paragraph contact =
                    new Paragraph(
                            contactDetails,
                            smallFont
                    );

            contact.setAlignment(Element.ALIGN_CENTER);
            document.add(contact);
        }

        String doctorName =
                fullName(
                        doctor.getFirstName(),
                        doctor.getLastName()
                );

        String doctorDetails =
                joinNonEmpty(
                        doctorName,
                        doctor.getQualification(),
                        doctor.getSpecialization()
                );

        if (!doctorDetails.isBlank()) {
            Paragraph doctorInfo =
                    new Paragraph(
                            doctorDetails,
                            smallFont
                    );

            doctorInfo.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(doctorInfo);
        }

        String registrationNumber =
                doctor.getRegistrationNumber();

        if (!isBlank(registrationNumber)) {
            Paragraph registration =
                    new Paragraph(
                            "Registration No.: " +
                                    registrationNumber,
                            smallFont
                    );

            registration.setAlignment(
                    Element.ALIGN_CENTER
            );

            document.add(registration);
        }

        document.add(
                new Paragraph(" ")
        );

        // ---------------------------------------------------------
        // Title
        // ---------------------------------------------------------

        Paragraph title =
                new Paragraph(
                        "PRESCRIPTION",
                        titleFont
                );

        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        document.add(
                new Paragraph(" ")
        );

        // ---------------------------------------------------------
        // Prescription / Patient Information
        // ---------------------------------------------------------

        PdfPTable patientTable =
                new PdfPTable(2);

        patientTable.setWidthPercentage(100);
        patientTable.setWidths(
                new float[]{1f, 1f}
        );

        addInfoCell(
                patientTable,
                "Prescription Date",
                prescription.getPrescriptionDate() != null
                        ? prescription.getPrescriptionDate().toString()
                        : "",
                normalFont
        );

        addInfoCell(
                patientTable,
                "Status",
                safe(prescription.getStatus()),
                normalFont
        );

        addInfoCell(
                patientTable,
                "Patient",
                fullName(
                        patient.getFirstName(),
                        patient.getLastName()
                ),
                normalFont
        );

        addInfoCell(
                patientTable,
                "Patient Number",
                safe(patient.getPatientNumber()),
                normalFont
        );

        addInfoCell(
                patientTable,
                "Mobile",
                safe(patient.getMobile()),
                normalFont
        );

        addInfoCell(
                patientTable,
                "Date of Birth",
                patient.getDateOfBirth() != null
                        ? patient.getDateOfBirth().toString()
                        : "",
                normalFont
        );

        addInfoCell(
                patientTable,
                "Gender",
                safe(patient.getGender()),
                normalFont
        );

        addInfoCell(
                patientTable,
                "Doctor",
                fullName(
                        doctor.getFirstName(),
                        doctor.getLastName()
                ),
                normalFont
        );

        document.add(patientTable);

        document.add(
                new Paragraph(" ")
        );

        // ---------------------------------------------------------
        // Doctor Details
        // ---------------------------------------------------------

        Paragraph doctorHeading =
                new Paragraph(
                        "Doctor Details",
                        sectionFont
                );

        document.add(doctorHeading);

        PdfPTable doctorTable =
                new PdfPTable(2);

        doctorTable.setWidthPercentage(100);
        doctorTable.setWidths(
                new float[]{1f, 1f}
        );

        addInfoCell(
                doctorTable,
                "Name",
                fullName(
                        doctor.getFirstName(),
                        doctor.getLastName()
                ),
                normalFont
        );

        addInfoCell(
                doctorTable,
                "Qualification",
                safe(doctor.getQualification()),
                normalFont
        );

        addInfoCell(
                doctorTable,
                "Specialization",
                safe(doctor.getSpecialization()),
                normalFont
        );

        addInfoCell(
                doctorTable,
                "Registration No.",
                safe(doctor.getRegistrationNumber()),
                normalFont
        );

        document.add(doctorTable);

        document.add(
                new Paragraph(" ")
        );

        // ---------------------------------------------------------
        // Diagnosis
        // ---------------------------------------------------------

        if (!isBlank(prescription.getDiagnosis())) {

            Paragraph diagnosisHeading =
                    new Paragraph(
                            "Diagnosis",
                            sectionFont
                    );

            document.add(diagnosisHeading);

            document.add(
                    new Paragraph(
                            safe(prescription.getDiagnosis()),
                            normalFont
                    )
            );

            document.add(
                    new Paragraph(" ")
            );
        }

        // ---------------------------------------------------------
        // Medicines
        // ---------------------------------------------------------

        Paragraph medicinesHeading =
                new Paragraph(
                        "Medicines",
                        sectionFont
                );

        document.add(medicinesHeading);

        PdfPTable medicineTable =
                new PdfPTable(9);

        medicineTable.setWidthPercentage(100);

        medicineTable.setWidths(
                new float[]{
                        0.35f,
                        1.35f,
                        0.85f,
                        0.85f,
                        0.85f,
                        0.75f,
                        0.65f,
                        0.75f,
                        1.25f
                }
        );

        addHeaderCell(
                medicineTable,
                "#",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Medicine",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Type",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Dosage",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Frequency",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Duration",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Route",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Quantity",
                tableHeaderFont
        );

        addHeaderCell(
                medicineTable,
                "Instructions",
                tableHeaderFont
        );

        int index = 1;

        for (PrescriptionItem item :
                prescription.getItems()) {

            addTableCell(
                    medicineTable,
                    String.valueOf(index++),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getMedicineName()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getMedicineType()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getDosage()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getFrequency()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getDuration()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getRoute()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getQuantity()),
                    smallFont
            );

            addTableCell(
                    medicineTable,
                    safe(item.getInstructions()),
                    smallFont
            );
        }

        document.add(medicineTable);

        // ---------------------------------------------------------
        // Notes
        // ---------------------------------------------------------

        if (!isBlank(prescription.getNotes())) {

            document.add(
                    new Paragraph(" ")
            );

            Paragraph notesHeading =
                    new Paragraph(
                            "Notes",
                            sectionFont
                    );

            document.add(notesHeading);

            document.add(
                    new Paragraph(
                            safe(prescription.getNotes()),
                            normalFont
                    )
            );
        }

        // ---------------------------------------------------------
        // Signature
        // ---------------------------------------------------------

        document.add(
                new Paragraph(" ")
        );

        document.add(
                new Paragraph(" ")
        );

        PdfPTable signatureTable =
                new PdfPTable(2);

        signatureTable.setWidthPercentage(100);
        signatureTable.setWidths(
                new float[]{1f, 1f}
        );

        PdfPCell emptyCell =
                new PdfPCell(
                        new Phrase("")
                );

        emptyCell.setBorder(
                PdfPCell.NO_BORDER
        );

        signatureTable.addCell(emptyCell);

        PdfPCell signatureCell =
                new PdfPCell(
                        new Phrase(
                                "____________________________\n"
                                        + "Doctor Signature",
                                normalFont
                        )
                );

        signatureCell.setBorder(
                PdfPCell.NO_BORDER
        );

        signatureCell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        signatureTable.addCell(signatureCell);

        document.add(signatureTable);

        document.add(
                new Paragraph(" ")
        );



        document.close();

        return outputStream.toByteArray();
    }

    private void addInfoCell(
            PdfPTable table,
            String label,
            String value,
            Font font
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                label + ": " + safe(value),
                                font
                        )
                );

        cell.setPadding(5);
        table.addCell(cell);
    }

    private void addHeaderCell(
            PdfPTable table,
            String value,
            Font font
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                value,
                                font
                        )
                );

        cell.setHorizontalAlignment(
                Element.ALIGN_CENTER
        );

        cell.setVerticalAlignment(
                Element.ALIGN_MIDDLE
        );

        cell.setPadding(4);

        table.addCell(cell);
    }

    private void addTableCell(
            PdfPTable table,
            String value,
            Font font
    ) {
        PdfPCell cell =
                new PdfPCell(
                        new Phrase(
                                safe(value),
                                font
                        )
                );

        cell.setPadding(3);
        cell.setVerticalAlignment(
                Element.ALIGN_TOP
        );

        table.addCell(cell);
    }

    private String fullName(
            String firstName,
            String lastName
    ) {
        return joinNonEmpty(
                firstName,
                lastName
        );
    }

    private String joinNonEmpty(
            String... values
    ) {
        StringBuilder result =
                new StringBuilder();

        for (String value : values) {

            if (isBlank(value)) {
                continue;
            }

            if (!result.isEmpty()) {
                result.append(", ");
            }

            result.append(value.trim());
        }

        return result.toString();
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private boolean isBlank(String value) {
        return value == null ||
                value.trim().isEmpty();
    }

    private void validateTenant(UUID tenantId) {
        if (tenantId == null) {
            throw new IllegalArgumentException(
                    "Tenant ID is required"
            );
        }
    }
}