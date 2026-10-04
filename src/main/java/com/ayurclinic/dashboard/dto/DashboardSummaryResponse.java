package com.ayurclinic.dashboard.dto;

import java.math.BigDecimal;

public class DashboardSummaryResponse {

    private long totalDoctors;
    private long activeDoctors;
    private long totalPatients;

    private long todayAppointments;
    private long todayRequestedAppointments;
    private long todayConfirmedAppointments;
    private long todayCompletedAppointments;
    private long todayCancelledAppointments;
    private long todayNoShowAppointments;
    private long todayFollowUps;
    private long todayScheduledFollowUps;
    private long todayCompletedFollowUps;
    private long todayCompletedConsultations;
    private BigDecimal todayRevenue;
    private BigDecimal outstandingAmount;


    public DashboardSummaryResponse() {
    }

    public DashboardSummaryResponse(
            long totalDoctors,
            long activeDoctors,
            long totalPatients,
            long todayAppointments,
            long todayRequestedAppointments,
            long todayConfirmedAppointments,
            long todayCompletedAppointments,
            long todayCancelledAppointments,
            long todayNoShowAppointments,
            long todayFollowUps,
            long todayScheduledFollowUps,
            long todayCompletedFollowUps,
            long todayCompletedConsultations,
            BigDecimal todayRevenue,
            BigDecimal outstandingAmount

    ) {
        this.totalDoctors = totalDoctors;
        this.activeDoctors = activeDoctors;
        this.totalPatients = totalPatients;
        this.todayAppointments = todayAppointments;
        this.todayRequestedAppointments = todayRequestedAppointments;
        this.todayConfirmedAppointments = todayConfirmedAppointments;
        this.todayCompletedAppointments = todayCompletedAppointments;
        this.todayCancelledAppointments = todayCancelledAppointments;
        this.todayNoShowAppointments = todayNoShowAppointments;
        this.todayFollowUps = todayFollowUps;
        this.todayScheduledFollowUps = todayScheduledFollowUps;
        this.todayCompletedFollowUps = todayCompletedFollowUps;
        this.todayCompletedConsultations = todayCompletedConsultations;
        this.todayRevenue = todayRevenue;
        this.outstandingAmount = outstandingAmount;
    }

    public long getTotalDoctors() {
        return totalDoctors;
    }

    public void setTotalDoctors(long totalDoctors) {
        this.totalDoctors = totalDoctors;
    }

    public long getActiveDoctors() {
        return activeDoctors;
    }

    public void setActiveDoctors(long activeDoctors) {
        this.activeDoctors = activeDoctors;
    }

    public long getTotalPatients() {
        return totalPatients;
    }

    public void setTotalPatients(long totalPatients) {
        this.totalPatients = totalPatients;
    }

    public long getTodayAppointments() {
        return todayAppointments;
    }

    public void setTodayAppointments(long todayAppointments) {
        this.todayAppointments = todayAppointments;
    }

    public long getTodayRequestedAppointments() {
        return todayRequestedAppointments;
    }

    public void setTodayRequestedAppointments(long todayRequestedAppointments) {
        this.todayRequestedAppointments = todayRequestedAppointments;
    }

    public long getTodayConfirmedAppointments() {
        return todayConfirmedAppointments;
    }

    public void setTodayConfirmedAppointments(long todayConfirmedAppointments) {
        this.todayConfirmedAppointments = todayConfirmedAppointments;
    }

    public long getTodayCompletedAppointments() {
        return todayCompletedAppointments;
    }

    public void setTodayCompletedAppointments(long todayCompletedAppointments) {
        this.todayCompletedAppointments = todayCompletedAppointments;
    }

    public long getTodayCancelledAppointments() {
        return todayCancelledAppointments;
    }

    public void setTodayCancelledAppointments(long todayCancelledAppointments) {
        this.todayCancelledAppointments = todayCancelledAppointments;
    }

    public long getTodayNoShowAppointments() {
        return todayNoShowAppointments;
    }

    public void setTodayNoShowAppointments(long todayNoShowAppointments) {
        this.todayNoShowAppointments = todayNoShowAppointments;
    }

    public long getTodayFollowUps() {
        return todayFollowUps;
    }

    public void setTodayFollowUps(long todayFollowUps) {
        this.todayFollowUps = todayFollowUps;
    }

    public long getTodayScheduledFollowUps() {
        return todayScheduledFollowUps;
    }

    public void setTodayScheduledFollowUps(long todayScheduledFollowUps) {
        this.todayScheduledFollowUps = todayScheduledFollowUps;
    }

    public long getTodayCompletedFollowUps() {
        return todayCompletedFollowUps;
    }

    public void setTodayCompletedFollowUps(long todayCompletedFollowUps) {
        this.todayCompletedFollowUps = todayCompletedFollowUps;
    }

    public long getTodayCompletedConsultations() {
        return todayCompletedConsultations;
    }

    public void setTodayCompletedConsultations(long todayCompletedConsultations) {
        this.todayCompletedConsultations = todayCompletedConsultations;
    }

    public BigDecimal getTodayRevenue() {
        return todayRevenue;
    }

    public void setTodayRevenue(BigDecimal todayRevenue) {
        this.todayRevenue = todayRevenue;
    }

    public BigDecimal getOutstandingAmount() {
        return outstandingAmount;
    }

    public void setOutstandingAmount(BigDecimal outstandingAmount) {
        this.outstandingAmount = outstandingAmount;
    }
}