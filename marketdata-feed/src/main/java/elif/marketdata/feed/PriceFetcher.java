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

    private CoinPriceDto createCoinDto(JsonNode jsonCoin, String symbol) {
        CoinPriceDto coinPriceDto = new CoinPriceDto(symbol);
        coinPriceDto.setAskPrice(new BigDecimal(jsonCoin.get("askPrice").asText()));
        coinPriceDto.setBidPrice(new BigDecimal(jsonCoin.get("bidPrice").asText()));
        coinPriceDto.setLastPrice(new BigDecimal(jsonCoin.get("lastPrice").asText()));
        coinPriceDto.setVolume(new BigDecimal(jsonCoin.get("volume").asText()));
        return coinPriceDto;
    }

    private CoinPriceBase createCoinEntity(CoinPriceDto coinPriceDto, long time, String entityType) {
        CoinPriceBase coinPrice;
        switch (entityType) {
            case "4minute":
                coinPrice = new CoinPrice4Minute(coinPriceDto.getSymbol());
                break;
            case "hour":
                coinPrice = new CoinPriceHour(coinPriceDto.getSymbol());
                break;
            case "2hour":
                coinPrice = new CoinPrice2Hour(coinPriceDto.getSymbol());
                break;
            case "day":
                coinPrice = new CoinPriceDay(coinPriceDto.getSymbol());
                break;
            default:
                coinPrice = new CoinPriceBase();
        }
        coinPrice.setAskPrice(coinPriceDto.getAskPrice());
        coinPrice.setBidPrice(coinPriceDto.getBidPrice());
        coinPrice.setLastPrice(coinPriceDto.getLastPrice());
        coinPrice.setVolume(coinPriceDto.getVolume());
        coinPrice.setTime(time);
        return coinPrice;
    }

    private void saveCoinToDB(CoinPriceDto coinPrice) {
        saveCoinTo4MinTable(coinPrice);
        saveCoinToHourTable(coinPrice);
        saveCoinTo2HourTable(coinPrice);
        saveCoinToDayTable(coinPrice);
    }

    private long[] getOrCreateLastTimes(CoinPriceDto coinPrice) {
        return coinLastPriceTimeMap.getOrDefault(coinPrice.getSymbol(), new long[4]);
    }

    private void publishHistoricPricesToHZC(String key, List<CoinPriceBase> coinPrices) {
        List<CoinPriceDto> list = new ArrayList<>();
        coinPrices.forEach(price -> {
            list.add(new CoinPriceDto(price));
        });
        String jsonStr = "";
        try {
            jsonStr = mapper.writer().writeValueAsString(list);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        Map<String, String> hzCoinChartMap = hc.getCoinChartDataMap();
        hzCoinChartMap.put(key, jsonStr);
    }

    private void saveCoinTo4MinTable(CoinPriceDto coinPriceDto) {
        long now = Instant.now().truncatedTo(ChronoUnit.MINUTES).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPriceDto);
        long last4Min = lastPriceTimesForCoin[0];
        if (now >= Instant.ofEpochMilli(last4Min).plus(4, ChronoUnit.MINUTES).toEpochMilli()) {
            // save coin to 4min table
            coinPrice4MinuteDao.save((CoinPrice4Minute) createCoinEntity(coinPriceDto, now, "4minute"));
            // update last price
            lastPriceTimesForCoin[0] = now;
            // update cache for the last 4 min time
            coinLastPriceTimeMap.put(coinPriceDto.getSymbol(), lastPriceTimesForCoin);
            // publish last daily prices in hzc map
            String key = coinPriceDto.getSymbol() + "_daily";
            // go to db, select 360 records,
            List<CoinPriceBase> coinPrice4Minutes = coinPrice4MinuteDao.findFirst360BySymbolOrderByAddTimeDesc(coinPriceDto.getSymbol());
            // create List<CP>, convert to JSONString and put in HZC
            publishHistoricPricesToHZC(key, coinPrice4Minutes);
            System.out.println("last 4 min price added to db and cache. Time for " + coinPriceDto.getSymbol() + " :" + now);
        }
    }

    private void saveCoinToHourTable(CoinPriceDto coinPriceDto) {
        long now = Instant.now().truncatedTo(ChronoUnit.HOURS).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPriceDto);
        long lastHour = lastPriceTimesForCoin[1];
        if (now >= Instant.ofEpochMilli(lastHour).plus(1, ChronoUnit.HOURS).toEpochMilli()) {
            coinPriceHourDao.save((CoinPriceHour) createCoinEntity(coinPriceDto, now, "hour"));
            lastPriceTimesForCoin[1] = now;
            // update cache for the last hour price
            coinLastPriceTimeMap.put(coinPriceDto.getSymbol(), lastPriceTimesForCoin);
            String key = coinPriceDto.getSymbol() + "_weekly";
            List<CoinPriceBase> coinPriceHours = coinPriceHourDao.findFirst200BySymbolOrderByAddTimeDesc(coinPriceDto.getSymbol());
            publishHistoricPricesToHZC(key, coinPriceHours);
            System.out.println("last hour price added to db and weekly cache. Time for " + coinPriceDto.getSymbol() + " :" + now);
        }
    }

    private void saveCoinTo2HourTable(CoinPriceDto coinPriceDto) {
        long now = Instant.now().truncatedTo(ChronoUnit.HOURS).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPriceDto);
        long lastHour = lastPriceTimesForCoin[2];
        if (now >= Instant.ofEpochMilli(lastHour).plus(2, ChronoUnit.HOURS).toEpochMilli()) {
            coinPrice2HourDao.save((CoinPrice2Hour) createCoinEntity(coinPriceDto, now, "2hour"));
            lastPriceTimesForCoin[2] = now;
            // update cache for the last 4 min time
            coinLastPriceTimeMap.put(coinPriceDto.getSymbol(), lastPriceTimesForCoin);
            String key = coinPriceDto.getSymbol() + "_monthly";
            List<CoinPriceBase> coinPrice2Hours = coinPrice2HourDao.findFirst360BySymbolOrderByAddTimeDesc(coinPriceDto.getSymbol());
            publishHistoricPricesToHZC(key, coinPrice2Hours);
            System.out.println("last 2 hour price added to db and monthly cache. Time for " + coinPriceDto.getSymbol() + " :" + now);
        }
    }

    private void saveCoinToDayTable(CoinPriceDto coinPriceDto) {
        long now = Instant.now().truncatedTo(ChronoUnit.DAYS).toEpochMilli();
        // get the latest minute record from the map  ???
        long[] lastPriceTimesForCoin = getOrCreateLastTimes(coinPriceDto);
        long lastHour = lastPriceTimesForCoin[3];
        if (now >= Instant.ofEpochMilli(lastHour).plus(1, ChronoUnit.DAYS).toEpochMilli()) {
            coinPriceDayDao.save((CoinPriceDay) createCoinEntity(coinPriceDto, now, "day"));
            lastPriceTimesForCoin[3] = now;
            // update cache for the last 4 min time
            coinLastPriceTimeMap.put(coinPriceDto.getSymbol(), lastPriceTimesForCoin);
            String key = coinPriceDto.getSymbol() + "_yearly";
            List<CoinPriceBase> coinPriceDays = coinPriceDayDao.findFirst360BySymbolOrderByAddTimeDesc(coinPriceDto.getSymbol());
            publishHistoricPricesToHZC(key, coinPriceDays);
            System.out.println("last 1 day price added to db and yearly cache. Time for " + coinPriceDto.getSymbol() + " :" + now);
        }
    }

    private void saveCoin(JsonNode jsonCoin) {
        String symbolUSDC = jsonCoin.get("symbol").asText();
        if (coinSymbolSet.contains(symbolUSDC)) {
            String symbol = symbolUSDC.substring(0, symbolUSDC.length() - 4);
            // cnstruct map
            final CoinPriceDto coinPrice = createCoinDto(jsonCoin, symbol);
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
