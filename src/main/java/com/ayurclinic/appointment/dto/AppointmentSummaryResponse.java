package com.ayurclinic.appointment.dto;

import java.time.LocalDate;

public class AppointmentSummaryResponse {

    private LocalDate date;

    private long total;
    private long requested;
    private long confirmed;
    private long completed;
    private long cancelled;
    private long noShow;

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getRequested() {
        return requested;
    }

    public void setRequested(long requested) {
        this.requested = requested;
    }

    public long getConfirmed() {
        return confirmed;
    }

    public void setConfirmed(long confirmed) {
        this.confirmed = confirmed;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getCancelled() {
        return cancelled;
    }

    public void setCancelled(long cancelled) {
        this.cancelled = cancelled;
    }

    public long getNoShow() {
        return noShow;
    }

    public void setNoShow(long noShow) {
        this.noShow = noShow;
    }
}