CREATE TABLE documents (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL
        REFERENCES tenants(id),

    clinic_id UUID NOT NULL
        REFERENCES clinics(id),

    patient_id UUID NOT NULL
        REFERENCES patients(id),

    consultation_id UUID NULL
        REFERENCES consultations(id),

    followup_id UUID NULL
        REFERENCES followups(id),

    document_type VARCHAR(50) NOT NULL,

    original_file_name VARCHAR(255) NOT NULL,

    storage_key VARCHAR(500) NOT NULL,

    content_type VARCHAR(100) NOT NULL,

    file_size BIGINT NOT NULL,

    description VARCHAR(1000),

    uploaded_by UUID,

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_documents_file_size
        CHECK (file_size >= 0),

    CONSTRAINT uq_documents_storage_key
        UNIQUE (storage_key)
);

CREATE INDEX idx_documents_tenant
    ON documents(tenant_id);

CREATE INDEX idx_documents_patient
    ON documents(patient_id);

CREATE INDEX idx_documents_consultation
    ON documents(consultation_id);

CREATE INDEX idx_documents_followup
    ON documents(followup_id);

CREATE INDEX idx_documents_clinic
    ON documents(clinic_id);

CREATE INDEX idx_documents_created_at
    ON documents(created_at);
