package com.task.tradingAutomation.repository;

import com.task.tradingAutomation.entity.Trades;
import com.task.tradingAutomation.enums.TradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trades, Long> {

    @Query(value = "SELECT * FROM trade_automation.trades t WHERE t.symbol_id = ?1 and t.status =?2 ORDER BY t.updated_at DESC LIMIT 1", nativeQuery = true)
    Trades findLatestBySymbolIdAndStatus(String symbolId,String status);

    @Query("SELECT t FROM Trades t WHERE t.status = :status")
    List<Trades> findByStatus(TradeStatus status);
}
