ALTER TABLE appointments
    ADD COLUMN booking_reference VARCHAR(30);

CREATE UNIQUE INDEX uq_appointments_booking_reference
    ON appointments (booking_reference);