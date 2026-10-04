CREATE TABLE patient_history (
                                 id UUID PRIMARY KEY,
                                 tenant_id UUID NOT NULL REFERENCES tenants(id),
                                 patient_id UUID NOT NULL REFERENCES patients(id),

                                 history_type VARCHAR(50) NOT NULL,
                                 title VARCHAR(200),
                                 description TEXT,

                                 recorded_at TIMESTAMPTZ NOT NULL,

                                 created_at TIMESTAMPTZ NOT NULL,
                                 updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_patient_history_tenant_patient
    ON patient_history(tenant_id, patient_id);

CREATE INDEX idx_patient_history_patient_recorded_at
    ON patient_history(patient_id, recorded_at DESC);