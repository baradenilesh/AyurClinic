CREATE UNIQUE INDEX uq_clinics_one_per_tenant
    ON clinics (tenant_id);