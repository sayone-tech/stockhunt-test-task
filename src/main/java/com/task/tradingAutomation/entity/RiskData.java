package com.task.tradingAutomation.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "RiskData")
public class RiskData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cumulative_risk", nullable = false)
    private Float cumulativeRisk;

    @Column(name = "date", nullable = false)
    private LocalDateTime date; // To track the date of the risk data

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public Float getCumulativeRisk() {
        return cumulativeRisk;
    }

    public void setCumulativeRisk(Float cumulativeRisk) {
        this.cumulativeRisk = cumulativeRisk;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}