package elif.marketdata.common.jpa;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
public interface CoinPrice4MinuteDao extends CrudRepository<CoinPrice4Minute, Long>, JpaSpecificationExecutor {

    // Get daily prices
    List<CoinPrice4Minute> findFirst360BySymbolOrderByAddTimeDesc(String symbol);


    CoinPrice4Minute findFirst1BySymbolOrderByAddTimeDesc(String symbol);
}
