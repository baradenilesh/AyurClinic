ALTER TABLE notifications
DROP CONSTRAINT chk_notification_type;

ALTER TABLE notifications
    ADD CONSTRAINT chk_notification_type
        CHECK (
            type IN (
                     'FOLLOW_UP_REMINDER',
                     'APPOINTMENT_CONFIRMATION',
                     'APPOINTMENT_REMINDER',
                     'APPOINTMENT_CANCELLED',
                     'APPOINTMENT_RESCHEDULED',
                     'PRESCRIPTION_READY',
                     'INVOICE_ISSUED',
                     'PAYMENT_RECEIVED'
                )
            );