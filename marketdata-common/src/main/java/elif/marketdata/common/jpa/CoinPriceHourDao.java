package elif.marketdata.common.jpa;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CoinPriceHourDao extends CrudRepository<CoinPriceHour, Long>, JpaSpecificationExecutor {
    // Get weekly prices
    List<CoinPriceHour> findFirst200BySymbolOrderByAddTimeDesc(String symbol);
}
