CREATE TABLE patient_number_counters (
                                         tenant_id UUID PRIMARY KEY REFERENCES tenants(id),
                                         next_number BIGINT NOT NULL DEFAULT 1
);
