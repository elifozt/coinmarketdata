package elif.marketdata.feed;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.HazelcastClientService;
import elif.marketdata.common.jpa.CoinPrice;
import elif.marketdata.common.jpa.CoinPriceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
public class PriceFetcher {
    @Autowired
    HazelcastClientService hc;
    @Autowired
    CoinPriceDao coinPriceDao;

    private ScheduledExecutorService ses;

    private static final Logger log = LoggerFactory.getLogger(MarketDataFeedApplication.class);
    private final Map<String, CoinPriceDto> coinPriceMap = new HashMap<>(10);

    private CoinPriceDto saveCoinDto(JsonNode coin, String symbol) {
        CoinPriceDto coinPrice = new CoinPriceDto(symbol);
        coinPrice.setAskPrice(new BigDecimal(coin.get("askPrice").asText()));
        coinPrice.setBidPrice(new BigDecimal(coin.get("bidPrice").asText()));
        coinPrice.setLastPrice(new BigDecimal(coin.get("lastPrice").asText()));
        coinPrice.setVolume(new BigDecimal(coin.get("volume").asText()));
        coinPriceMap.put(coinPrice.getSymbol(), coinPrice);
        log.info("Coin is saved to in map db");
        return coinPrice;
    }

    private void saveCoinDao(CoinPriceDto coinPrice) {
        //saveCoinPriceEntity()
        CoinPrice coinPriceEntity = new CoinPrice(coinPrice.getSymbol());
        coinPriceEntity.setAskPrice(coinPrice.getAskPrice());
        coinPriceEntity.setBidPrice(coinPrice.getBidPrice());
        coinPriceEntity.setLastPrice(coinPrice.getLastPrice());
        coinPriceEntity.setVolume(coinPrice.getVolume());
        coinPriceDao.save(coinPriceEntity);
        log.info("Coin is saved to in mysql db");
    }

    private void saveCoin(JsonNode coin) {
        String symbolUSDC = coin.get("symbol").asText();
        if (symbolUSDC.equals("BTCUSDC") || symbolUSDC.equals("ETHUSDC") || symbolUSDC.equals("LTCUSDC") ||
                symbolUSDC.equals("XRPUSDC")) {
            String symbol = symbolUSDC.substring(0, symbolUSDC.length() - 4);
            // cnstruct map
            CoinPriceDto coinPrice = saveCoinDto(coin, symbol);
            //save into db
            saveCoinDao(coinPrice);
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
