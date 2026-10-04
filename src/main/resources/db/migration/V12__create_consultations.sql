CREATE TABLE consultations (
                               id UUID PRIMARY KEY,

                               tenant_id UUID NOT NULL,
                               clinic_id UUID NOT NULL,
                               appointment_id UUID NOT NULL,
                               patient_id UUID NOT NULL,
                               doctor_id UUID NOT NULL,

                               consultation_date DATE NOT NULL,

                               chief_complaint TEXT,
                               symptoms TEXT,
                               clinical_findings TEXT,
                               diagnosis TEXT,
                               treatment_plan TEXT,
                               doctor_notes TEXT,

                               status VARCHAR(30) NOT NULL DEFAULT 'IN_PROGRESS',

                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_consultation_tenant
                                   FOREIGN KEY (tenant_id)
                                       REFERENCES tenants(id),

                               CONSTRAINT fk_consultation_clinic
                                   FOREIGN KEY (clinic_id)
                                       REFERENCES clinics(id),

                               CONSTRAINT fk_consultation_appointment
                                   FOREIGN KEY (appointment_id)
                                       REFERENCES appointments(id),

                               CONSTRAINT fk_consultation_patient
                                   FOREIGN KEY (patient_id)
                                       REFERENCES patients(id),

                               CONSTRAINT fk_consultation_doctor
                                   FOREIGN KEY (doctor_id)
                                       REFERENCES doctors(id),

                               CONSTRAINT uq_consultation_appointment
                                   UNIQUE (appointment_id),

                               CONSTRAINT chk_consultation_status
                                   CHECK (
                                       status IN (
                                                  'IN_PROGRESS',
                                                  'COMPLETED'
                                           )
                                       )
);

CREATE INDEX idx_consultations_tenant_id
    ON consultations(tenant_id);

CREATE INDEX idx_consultations_patient_id
    ON consultations(patient_id);

CREATE INDEX idx_consultations_doctor_id
    ON consultations(doctor_id);

CREATE INDEX idx_consultations_clinic_id
    ON consultations(clinic_id);

CREATE INDEX idx_consultations_date
    ON consultations(consultation_date);