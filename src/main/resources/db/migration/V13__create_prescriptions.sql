CREATE TABLE prescriptions (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL,
    clinic_id UUID NOT NULL,
    consultation_id UUID NOT NULL,
    appointment_id UUID NOT NULL,
    patient_id UUID NOT NULL,
    doctor_id UUID NOT NULL,

    prescription_date DATE NOT NULL,

    diagnosis TEXT,
    notes TEXT,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescription_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_prescription_clinic
        FOREIGN KEY (clinic_id)
        REFERENCES clinics(id),

    CONSTRAINT fk_prescription_consultation
        FOREIGN KEY (consultation_id)
        REFERENCES consultations(id),

    CONSTRAINT fk_prescription_appointment
        FOREIGN KEY (appointment_id)
        REFERENCES appointments(id),

    CONSTRAINT fk_prescription_patient
        FOREIGN KEY (patient_id)
        REFERENCES patients(id),

    CONSTRAINT fk_prescription_doctor
        FOREIGN KEY (doctor_id)
        REFERENCES doctors(id),

    CONSTRAINT uq_prescription_consultation
        UNIQUE (consultation_id),

    CONSTRAINT chk_prescription_status
        CHECK (status IN ('DRAFT', 'ISSUED'))
);


CREATE TABLE prescription_items (
    id UUID PRIMARY KEY,
    prescription_id UUID NOT NULL,

    medicine_name VARCHAR(255) NOT NULL,
    medicine_type VARCHAR(100),

    dosage VARCHAR(100),
    frequency VARCHAR(100),
    duration VARCHAR(100),

    route VARCHAR(100),
    instructions TEXT,

    quantity VARCHAR(100),

    sort_order INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_prescription_item_prescription
        FOREIGN KEY (prescription_id)
        REFERENCES prescriptions(id)
        ON DELETE CASCADE
);


CREATE INDEX idx_prescriptions_tenant_id
    ON prescriptions(tenant_id);

CREATE INDEX idx_prescriptions_clinic_id
    ON prescriptions(clinic_id);

CREATE INDEX idx_prescriptions_consultation_id
    ON prescriptions(consultation_id);

CREATE INDEX idx_prescriptions_patient_id
    ON prescriptions(patient_id);

CREATE INDEX idx_prescriptions_doctor_id
    ON prescriptions(doctor_id);

CREATE INDEX idx_prescriptions_date
    ON prescriptions(prescription_date);

CREATE INDEX idx_prescription_items_prescription_id
    ON prescription_items(prescription_id);