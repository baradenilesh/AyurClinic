CREATE TABLE doctor_availability (
    id UUID PRIMARY KEY,
    tenant_id UUID NOT NULL REFERENCES tenants(id),
    doctor_id UUID NOT NULL REFERENCES doctors(id),
    day_of_week SMALLINT NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    slot_duration_minutes INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT chk_day_of_week CHECK (day_of_week BETWEEN 1 AND 7),
    CONSTRAINT chk_slot_duration CHECK (slot_duration_minutes > 0),
    CONSTRAINT chk_availability_time CHECK (start_time < end_time)
);

CREATE INDEX idx_doctor_availability_tenant_doctor
    ON doctor_availability(tenant_id, doctor_id, day_of_week);
