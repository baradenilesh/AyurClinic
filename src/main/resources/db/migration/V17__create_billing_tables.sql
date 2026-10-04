CREATE TABLE invoices (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL
        REFERENCES tenants(id),

    clinic_id UUID NOT NULL
        REFERENCES clinics(id),

    patient_id UUID NOT NULL
        REFERENCES patients(id),

    appointment_id UUID NULL
        REFERENCES appointments(id),

    consultation_id UUID NULL
        REFERENCES consultations(id),

    invoice_number VARCHAR(50) NOT NULL,

    invoice_date DATE NOT NULL,

    due_date DATE NULL,

    subtotal NUMERIC(12, 2) NOT NULL,

    discount NUMERIC(12, 2) NOT NULL DEFAULT 0,

    tax NUMERIC(12, 2) NOT NULL DEFAULT 0,

    total_amount NUMERIC(12, 2) NOT NULL,

    paid_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,

    status VARCHAR(30) NOT NULL DEFAULT 'DRAFT',

    notes VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT uq_invoice_tenant_number
        UNIQUE (tenant_id, invoice_number),

    CONSTRAINT chk_invoice_subtotal
        CHECK (subtotal >= 0),

    CONSTRAINT chk_invoice_discount
        CHECK (discount >= 0),

    CONSTRAINT chk_invoice_tax
        CHECK (tax >= 0),

    CONSTRAINT chk_invoice_total
        CHECK (total_amount >= 0),

    CONSTRAINT chk_invoice_paid_amount
        CHECK (paid_amount >= 0),

    CONSTRAINT chk_invoice_status
        CHECK (
            status IN (
                'DRAFT',
                'ISSUED',
                'PARTIALLY_PAID',
                'PAID',
                'CANCELLED'
            )
        )
);

CREATE INDEX idx_invoices_tenant
    ON invoices(tenant_id);

CREATE INDEX idx_invoices_clinic
    ON invoices(clinic_id);

CREATE INDEX idx_invoices_patient
    ON invoices(patient_id);

CREATE INDEX idx_invoices_appointment
    ON invoices(appointment_id);

CREATE INDEX idx_invoices_consultation
    ON invoices(consultation_id);

CREATE INDEX idx_invoices_status
    ON invoices(status);

CREATE INDEX idx_invoices_invoice_date
    ON invoices(invoice_date);


CREATE TABLE payments (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL
        REFERENCES tenants(id),

    clinic_id UUID NOT NULL
        REFERENCES clinics(id),

    patient_id UUID NOT NULL
        REFERENCES patients(id),

    invoice_id UUID NOT NULL
        REFERENCES invoices(id),

    amount NUMERIC(12, 2) NOT NULL,

    payment_date DATE NOT NULL,

    payment_method VARCHAR(30) NOT NULL,

    transaction_reference VARCHAR(255),

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    notes VARCHAR(1000),

    created_at TIMESTAMP WITH TIME ZONE NOT NULL,

    updated_at TIMESTAMP WITH TIME ZONE NOT NULL,

    CONSTRAINT chk_payment_amount
        CHECK (amount > 0),

    CONSTRAINT chk_payment_method
        CHECK (
            payment_method IN (
                'CASH',
                'UPI',
                'CARD',
                'BANK_TRANSFER',
                'OTHER'
            )
        ),

    CONSTRAINT chk_payment_status
        CHECK (
            status IN (
                'PENDING',
                'COMPLETED',
                'FAILED',
                'REFUNDED'
            )
        )
);

CREATE INDEX idx_payments_tenant
    ON payments(tenant_id);

CREATE INDEX idx_payments_clinic
    ON payments(clinic_id);

CREATE INDEX idx_payments_patient
    ON payments(patient_id);

CREATE INDEX idx_payments_invoice
    ON payments(invoice_id);

CREATE INDEX idx_payments_payment_date
    ON payments(payment_date);

CREATE INDEX idx_payments_status
    ON payments(status);