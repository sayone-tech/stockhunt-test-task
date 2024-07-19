package com.task.tradingAutomation.repository;


import com.task.tradingAutomation.entity.RiskData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RiskDataRepository extends JpaRepository<RiskData, Long> {

    @Query("SELECT r.cumulativeRisk FROM RiskData r WHERE r.date = (SELECT MAX(r2.date) FROM RiskData r2)")
    Optional<Float> findLatestCumulativeRisk();

}