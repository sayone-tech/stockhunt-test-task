package com.task.tradingAutomation.repository;


import com.task.tradingAutomation.entity.RiskData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface RiskDataRepository extends JpaRepository<RiskData, Long> {


    @Query("SELECT r.cumulativeRisk FROM RiskData r WHERE r.date >= :startOfDay AND r.date <= :endOfDay ORDER BY r.date DESC")
    Optional<Float> findLatestCumulativeRiskForToday(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);
}