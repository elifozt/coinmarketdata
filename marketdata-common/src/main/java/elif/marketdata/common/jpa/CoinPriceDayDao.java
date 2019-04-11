package elif.marketdata.common.jpa;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import javax.persistence.Table;
import java.util.List;

@Table ( name = "coin_price_day")
public interface CoinPriceDayDao extends CrudRepository<CoinPriceDay, Long>, JpaSpecificationExecutor {
    // Get daily prices
    List<CoinPriceDay> findFirst360BySymbolOrderByAddTimeDesc(String symbol);
}
