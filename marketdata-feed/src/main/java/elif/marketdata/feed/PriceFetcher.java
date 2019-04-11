package elif.marketdata.feed;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.HazelcastClientService;
import elif.marketdata.common.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URL;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class PriceFetcher {
    Set<String> coinSymbolSet = new HashSet<>(Arrays.asList("BTCUSDC", "ETHUSDC", "LTCUSDC", "XRPUSDC", "BNBUSDC", "EOSUSDC", "XLMUSDC", "TRXUSDC", "WAVESUSDC", "BCHABCUSDC", "BCHSVUSDC", "ZECUSDC"));

    @Autowired
    HazelcastClientService hc;
    @Autowired
    CoinPrice4MinuteDao coinPrice4MinuteDao;
    @Autowired
    CoinPriceHourDao coinPriceHourDao;
    @Autowired
    CoinPrice2HourDao coinPrice2HourDao;
    @Autowired
    CoinPriceDayDao coinPriceDayDao;

    private ScheduledExecutorService ses;

    private static final Logger log = LoggerFactory.getLogger(MarketDataFeedApplication.class);
    //live coin prices
    private final Map<String, CoinPriceDto> coinPriceMap = new HashMap<>(10);

    // holds the latest 4 minute, hour, 2hour, day  prices
    private final Map<String, long[]> coinLastPriceTimeMap = new HashMap<>(10);
    // is this thread-safe? yes. I read the documentation.
    private final ObjectMapper mapper = new ObjectMapper();

    private CoinPriceDto createCoinDto(JsonNode coin, String symbol) {
        CoinPriceDto coinPrice = new CoinPriceDto(symbol);
        coinPrice.setAskPrice(new BigDecimal(coin.get("askPrice").asText()));
        coinPrice.setBidPrice(new BigDecimal(coin.get("bidPrice").asText()));
        coinPrice.setLastPrice(new BigDecimal(coin.get("lastPrice").asText()));
        coinPrice.setVolume(new BigDecimal(coin.get("volume").asText()));
//        log.info("Coin is saved to in map db");
        return coinPrice;
    }

    private void saveCoinToDB(CoinPriceDto coinPrice) {
        // get the latest minute record from the map  ???
        saveCoinTo4MinTable(coinPrice);
        saveCoinToHourTable(coinPrice);
        saveCoinTo2HourTable(coinPrice);
        saveCoinToDayTable(coinPrice);
    }

    private long[] getOrCreateLastTimes(CoinPriceDto coinPrice) {
        return coinLastPriceTimeMap.getOrDefault(coinPrice.getSymbol(), new long[4]);
    }

    private void saveCoinTo4MinTable(CoinPriceDto coinPrice) {
        long now = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPrice);
        long last4Min = lastPriceTimesForCoin[0];
        if (last4Min == 0 ||
                now >= Instant.ofEpochMilli(last4Min).plus(4, ChronoUnit.MINUTES).toEpochMilli()) {
            CoinPrice4Minute coinPrice4MinEntity = new CoinPrice4Minute(coinPrice.getSymbol());
            coinPrice4MinEntity.setAskPrice(coinPrice.getAskPrice());
            coinPrice4MinEntity.setBidPrice(coinPrice.getBidPrice());
            coinPrice4MinEntity.setLastPrice(coinPrice.getLastPrice());
            coinPrice4MinEntity.setVolume(coinPrice.getVolume());
            coinPrice4MinEntity.setTime(now);
            coinPrice4MinuteDao.save(coinPrice4MinEntity);
            lastPriceTimesForCoin[0] = now;
            // update cache for the last 4 min time
            coinLastPriceTimeMap.put(coinPrice.getSymbol(), lastPriceTimesForCoin);
            String key = coinPrice.getSymbol() + "_daily";
            List<CoinPrice4Minute> coinPrice4Minutes = coinPrice4MinuteDao.findFirst360BySymbolOrderByAddTimeDesc(coinPrice.getSymbol());
            // go to db, select 360 records, create List<CP>, convert to JsonNode
            List<CoinPriceDto> list = new ArrayList<>();
            coinPrice4Minutes.forEach(price -> {
                list.add(new CoinPriceDto(price));
            } );
            String jsonStr ="";
            try {
                jsonStr = mapper.writer().writeValueAsString(list);
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            // JsonNode jsonNode =   null;
            // String value = jsonNode.toString();
            Map<String, String> hzCoinChartMap = hc.getCoinChartDataMap();
            hzCoinChartMap.put(key, jsonStr);

            System.out.println("last 4 min price added to db and cache. Time for " + coinPrice.getSymbol() + " :" + now);
        }
    }

    private void saveCoinToHourTable(CoinPriceDto coinPrice) {
        long now = Instant.now().truncatedTo(ChronoUnit.HOURS).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPrice);
        long lastHour = lastPriceTimesForCoin[1];
        if (now >= Instant.ofEpochMilli(lastHour).plus(1, ChronoUnit.HOURS).toEpochMilli()) {
            CoinPriceHour coinPriceHourEntity = new CoinPriceHour(coinPrice.getSymbol());
            coinPriceHourEntity.setAskPrice(coinPrice.getAskPrice());
            coinPriceHourEntity.setBidPrice(coinPrice.getBidPrice());
            coinPriceHourEntity.setLastPrice(coinPrice.getLastPrice());
            coinPriceHourEntity.setVolume(coinPrice.getVolume());
            coinPriceHourEntity.setTime(now);
            coinPriceHourDao.save(coinPriceHourEntity);
            lastPriceTimesForCoin[1] = now;
            // update cache for the last hour price
            coinLastPriceTimeMap.put(coinPrice.getSymbol(), lastPriceTimesForCoin);
            System.out.println("last hour price added to db and cache. Time for " + coinPrice.getSymbol() + " :" + now);
        }
    }

    private void saveCoinTo2HourTable(CoinPriceDto coinPrice) {
        long now = Instant.now().truncatedTo(ChronoUnit.HOURS).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPrice);
        long lastHour = lastPriceTimesForCoin[2];
        if (now >= Instant.ofEpochMilli(lastHour).plus(2, ChronoUnit.HOURS).toEpochMilli()) {
            CoinPrice2Hour coinPrice2HourEntity = new CoinPrice2Hour(coinPrice.getSymbol());
            coinPrice2HourEntity.setAskPrice(coinPrice.getAskPrice());
            coinPrice2HourEntity.setBidPrice(coinPrice.getBidPrice());
            coinPrice2HourEntity.setLastPrice(coinPrice.getLastPrice());
            coinPrice2HourEntity.setVolume(coinPrice.getVolume());
            coinPrice2HourEntity.setTime(now);
            coinPrice2HourDao.save(coinPrice2HourEntity);
            lastPriceTimesForCoin[2] = now;
            // update cache for the last 4 min time
            coinLastPriceTimeMap.put(coinPrice.getSymbol(), lastPriceTimesForCoin);
            System.out.println("last 2 hour price added to db and cache. Time for " + coinPrice.getSymbol() + " :" + now);
        }
    }

    private void saveCoinToDayTable(CoinPriceDto coinPrice) {
        long now = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPrice);
        long lastHour = lastPriceTimesForCoin[3];
        if (now >= Instant.ofEpochMilli(lastHour).plus(1, ChronoUnit.DAYS).toEpochMilli()) {
            CoinPriceDay coinPriceDayEntity = new CoinPriceDay(coinPrice.getSymbol());
            coinPriceDayEntity.setAskPrice(coinPrice.getAskPrice());
            coinPriceDayEntity.setBidPrice(coinPrice.getBidPrice());
            coinPriceDayEntity.setLastPrice(coinPrice.getLastPrice());
            coinPriceDayEntity.setVolume(coinPrice.getVolume());
            coinPriceDayEntity.setTime(now);
            coinPriceDayDao.save(coinPriceDayEntity);
            lastPriceTimesForCoin[3] = now;
            // update cache for the last 4 min time
            coinLastPriceTimeMap.put(coinPrice.getSymbol(), lastPriceTimesForCoin);
            System.out.println("last 1 day price added to db and cache. Time for " + coinPrice.getSymbol() + " :" + now);
        }
    }

    private void saveCoin(JsonNode coin) {
        String symbolUSDC = coin.get("symbol").asText();
        if (coinSymbolSet.contains(symbolUSDC)) {
            String symbol = symbolUSDC.substring(0, symbolUSDC.length() - 4);
            // cnstruct map
            final CoinPriceDto coinPrice = createCoinDto(coin, symbol);
            coinPriceMap.put(coinPrice.getSymbol(), coinPrice);
            //save into db
            saveCoinToDB(coinPrice);
        }
    }

    @PostConstruct
    public void init() throws Exception {
        final ObjectMapper objectMapper = new ObjectMapper();
        ses = Executors.newSingleThreadScheduledExecutor();
        System.out.println("INITIALIZING>>> ");
        ses.scheduleAtFixedRate(
                () -> {
                    try {
                        final JsonNode coins = objectMapper.readTree(new URL("https://api.binance.com/api/v1/ticker/24hr"));
                        coins.forEach(this::saveCoin);
                        hc.publish(coinPriceMap);
                        System.out.println("Published ");
                        log.info("Coins published hazelcast");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                },
                0,
                10,
                TimeUnit.SECONDS);
    }
}
