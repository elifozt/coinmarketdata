package elif.marketdata.common.jpa;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CoinPrice2HourDao extends CrudRepository<CoinPrice2Hour, Long>, JpaSpecificationExecutor {

    // Get monthly prices
    List<CoinPriceBase> findFirst360BySymbolOrderByAddTimeDesc(String symbol);
}
