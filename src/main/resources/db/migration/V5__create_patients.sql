CREATE TABLE patients (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    clinic_id UUID NOT NULL REFERENCES clinics(id),
    patient_number VARCHAR(50) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100),
    mobile VARCHAR(30) NOT NULL,
    email VARCHAR(150),
    date_of_birth DATE,
    gender VARCHAR(30),
    address TEXT,
    emergency_contact_name VARCHAR(200),
    emergency_contact_mobile VARCHAR(30),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT uq_patient_number UNIQUE (tenant_id, patient_number)
);

CREATE INDEX idx_patients_tenant_mobile ON patients(tenant_id, mobile);
CREATE INDEX idx_patients_tenant_name ON patients(tenant_id, first_name, last_name);
CREATE INDEX idx_patients_clinic ON patients(tenant_id, clinic_id);
