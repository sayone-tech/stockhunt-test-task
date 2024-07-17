package com.task.tradingAutomation.repository;

import com.task.tradingAutomation.Entity.Trades;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trades, Long> {

    @Query(value = "SELECT * FROM trade_automation.trades t WHERE t.symbol_id = ?1 ORDER BY t.updated_at DESC LIMIT 1", nativeQuery = true)
    Trades findLatestBySymbolId(String symbolId);
}
