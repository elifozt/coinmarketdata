package elif.marketdata.common.jpa;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
//import org.springframework.data.rest.core.annotation.RestResource;

@Transactional
public interface CoinPriceDao extends CrudRepository<CoinPrice, Long>, JpaSpecificationExecutor {

    List<CoinPrice> findFirst300BySymbolOrderByAddTimeDesc(String symbol);

    CoinPrice findFirst1BySymbolOrderByAddTimeDesc(String symbol);

    @Query(value = "SELECT *" +
            " FROM (SELECT symbol, MAX(add_time) as MaxTime FROM coin_price GROUP BY symbol) r" +
            " INNER JOIN coin_price t" +
            " ON t.symbol = r.symbol AND t.add_time = r.MaxTime", nativeQuery = true)
    List<CoinPrice> findLastPrices();
      /*
    @RestResource(exported = false)
    @Override
    <S extends CoinPrice> S save(S s);

    @RestResource(exported = false)
    @Override
    <S extends CoinPrice> Iterable<S> saveAll(Iterable<S> iterable);

    @RestResource(exported = false)
    @Override
    void deleteById(Long aLong);

    @RestResource(exported = false)
    @Override
    void delete(CoinPrice coinPrice);

    @RestResource(exported = false)
    @Override
    void deleteAll(Iterable<? extends CoinPrice> iterable);

    @RestResource(exported = false)
    @Override
    void deleteAll();  */
}

