package com.task.tradingAutomation.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "RiskData")
@Data
@NoArgsConstructor
public class RiskData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cumulative_risk", nullable = false)
    private Float cumulativeRisk;

    @Column(name = "date", nullable = false)
    private LocalDateTime date; // To track the date of the risk data
}