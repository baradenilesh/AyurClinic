CREATE TABLE doctors (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    clinic_id UUID NOT NULL REFERENCES clinics(id),
    user_id UUID REFERENCES users(id),
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    qualification VARCHAR(255),
    specialization VARCHAR(255),
    registration_number VARCHAR(100),
    experience_years INTEGER,
    bio TEXT,
    photo_s3_key VARCHAR(500),
    consultation_fee NUMERIC(12,2),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_doctors_tenant ON doctors(tenant_id);
CREATE INDEX idx_doctors_clinic ON doctors(tenant_id, clinic_id);
