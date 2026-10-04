CREATE TABLE notifications (
                               id UUID PRIMARY KEY,

                               tenant_id UUID NOT NULL
                                   REFERENCES tenants(id),

                               clinic_id UUID NOT NULL
                                   REFERENCES clinics(id),

                               patient_id UUID NOT NULL
                                   REFERENCES patients(id),

                               followup_id UUID
                                   REFERENCES followups(id),

                               type VARCHAR(50) NOT NULL,

                               channel VARCHAR(20) NOT NULL,

                               recipient VARCHAR(255) NOT NULL,

                               subject VARCHAR(255),

                               message TEXT NOT NULL,

                               scheduled_at TIMESTAMP WITH TIME ZONE,

                               sent_at TIMESTAMP WITH TIME ZONE,

                               status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

                               provider_message_id VARCHAR(255),

                               failure_reason TEXT,

                               created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT chk_notification_type
                                   CHECK (
                                       type IN (
                                           'FOLLOW_UP_REMINDER'
                                           )
                                       ),

                               CONSTRAINT chk_notification_channel
                                   CHECK (
                                       channel IN (
                                                   'WHATSAPP',
                                                   'SMS',
                                                   'EMAIL'
                                           )
                                       ),

                               CONSTRAINT chk_notification_status
                                   CHECK (
                                       status IN (
                                                  'PENDING',
                                                  'SENT',
                                                  'FAILED',
                                                  'CANCELLED'
                                           )
                                       )
);

CREATE INDEX idx_notifications_tenant
    ON notifications(tenant_id);

CREATE INDEX idx_notifications_clinic
    ON notifications(clinic_id);

CREATE INDEX idx_notifications_patient
    ON notifications(patient_id);

CREATE INDEX idx_notifications_followup
    ON notifications(followup_id);

CREATE INDEX idx_notifications_status
    ON notifications(status);

CREATE INDEX idx_notifications_scheduled_at
    ON notifications(scheduled_at);

CREATE INDEX idx_notifications_pending_scheduled
    ON notifications(status, scheduled_at);