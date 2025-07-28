package com.example.hospital_0515.model;

import javax.persistence.*;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Visit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String department;
    private LocalDateTime visitTime;
    private int availableSlots;
    private String status;
    private String doctorName;
    private boolean auction; // 新增字段

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> bookedBy = new ArrayList<>();

    private boolean recurring;
    @Enumerated(EnumType.STRING)
    private DayOfWeek recurringDayOfWeek;
    private LocalTime recurringVisitTime;

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public LocalDateTime getVisitTime() {
        return visitTime;
    }

    public void setVisitTime(LocalDateTime visitTime) {
        this.visitTime = visitTime;
    }

    public int getAvailableSlots() {
        return availableSlots;
    }

    public void setAvailableSlots(int availableSlots) {
        this.availableSlots = availableSlots;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getBookedBy() {
        return bookedBy;
    }

    public void setBookedBy(List<String> bookedBy) {
        this.bookedBy = bookedBy;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public DayOfWeek getRecurringDayOfWeek() {
        return recurringDayOfWeek;
    }

    public void setRecurringDayOfWeek(DayOfWeek recurringDayOfWeek) {
        this.recurringDayOfWeek = recurringDayOfWeek;
    }

    public LocalTime getRecurringVisitTime() {
        return recurringVisitTime;
    }

    public void setRecurringVisitTime(LocalTime recurringVisitTime) {
        this.recurringVisitTime = recurringVisitTime;
    }

    public boolean isAuction() {
        return auction;
    }

    public void setAuction(boolean auction) {
        this.auction = auction;
    }
}
