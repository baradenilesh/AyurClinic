CREATE TABLE followups (
                           id UUID PRIMARY KEY,

                           tenant_id UUID NOT NULL
                               REFERENCES tenants(id),

                           clinic_id UUID NOT NULL
                               REFERENCES clinics(id),

                           patient_id UUID NOT NULL
                               REFERENCES patients(id),

                           doctor_id UUID NOT NULL
                               REFERENCES doctors(id),

                           consultation_id UUID NOT NULL
                               REFERENCES consultations(id),

                           appointment_id UUID
                               REFERENCES appointments(id),

                           follow_up_date DATE NOT NULL,

                           reason VARCHAR(500),

                           notes TEXT,

                           status VARCHAR(30) NOT NULL
                               DEFAULT 'SCHEDULED',

                           reminder_sent BOOLEAN NOT NULL
                               DEFAULT FALSE,

                           created_at TIMESTAMPTZ NOT NULL,

                           updated_at TIMESTAMPTZ NOT NULL,

                           CONSTRAINT chk_followup_status
                               CHECK (
                                   status IN (
                                              'SCHEDULED',
                                              'COMPLETED',
                                              'CANCELLED',
                                              'MISSED'
                                       )
                                   )
);

CREATE INDEX idx_followups_tenant
    ON followups(tenant_id);

CREATE INDEX idx_followups_patient
    ON followups(tenant_id, patient_id);

CREATE INDEX idx_followups_doctor
    ON followups(tenant_id, doctor_id);

CREATE INDEX idx_followups_date
    ON followups(tenant_id, follow_up_date);

CREATE INDEX idx_followups_status
    ON followups(tenant_id, status);