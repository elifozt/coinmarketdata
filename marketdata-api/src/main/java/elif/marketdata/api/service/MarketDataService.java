package elif.marketdata.api.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import elif.marketdata.api.MarketDataApplication;
import elif.marketdata.api.websocket.WebSocketHandler;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.CoinPriceSet;
import elif.marketdata.common.HazelcastClientService;
import elif.marketdata.common.dto.ContactData;
import elif.marketdata.common.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class MarketDataService implements MessageListener<String> {
    @Autowired
    HazelcastClientService hc;
    @Autowired
    CoinPriceDao coinPriceDao;
    @Autowired
    WebSocketHandler webSocketHandler;
    @Autowired
    CoinChartDataService cds;
    @Value("${x.y}")
    private String xy;
    @Value("${a.b}")
    private String ab;

    private static final Logger log = LoggerFactory.getLogger(MarketDataApplication.class);

    // is this thread-safe? yes.. concurrenthashmap + we only put new CoinPrice objects
    private final Map<String, CoinPriceDto> lastPriceMap = new ConcurrentHashMap<>(10);
    // is this thread-safe? yes.. concurrenthashmap + we only put new CoinPrice objects
    private final Map<String, List<CoinPriceDto>> livePriceMap = new ConcurrentHashMap<>(12);
    // is this thread-safe? yes. I read the documentation.
    private final ObjectMapper mapper = new ObjectMapper();

    @PostConstruct
    public void init() {
        // Add listener to hc coinPrice topic
        hc.getPriceTopic().addMessageListener(this);
        System.out.println("Subscribed");
    }

    public void onMessage(Message<String> m) {
        //System.out.println("Received: " + m.getMessageObject());
        // message is jsonString
        String cpsString = m.getMessageObject();
        // new coinPrice is received
        CoinPriceSet cps = null;
        try {
            // read json string into CoinPriceSet object
            cps = mapper.readValue(cpsString, CoinPriceSet.class);
            cps.getCoinPrices().forEach(coinPrice -> {
                // put the prices on local cache - map
                lastPriceMap.put(coinPrice.getSymbol(), coinPrice);
                coinPrice.setAddTime(System.currentTimeMillis());
                addToLivePriceMap(coinPrice);
            });
            final String arrayStr = mapper.writer().writeValueAsString(cps.getCoinPrices());
            // create websocket json message
            webSocketHandler.sendToEveryone(arrayStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addToLivePriceMap(CoinPriceDto coinPrice) {
        List<CoinPriceDto> coinLiveList = livePriceMap.computeIfAbsent(coinPrice.getSymbol(), k -> new ArrayList<>());
        if (coinLiveList.size() >= 360) {
            coinLiveList.remove(0);
        }
        coinLiveList.add(coinPrice);
        log.info("price added to live prices of :" + coinPrice.getSymbol());
    }

    public List<CoinPriceDto> getCoinPriceLive(String symbol) {
        return livePriceMap.get(symbol);
    }

    // Get all prices of a symbol
    public List<CoinPriceDto> getPrice(String coinSymbol) {
        final List<CoinPrice> coinPriceList = coinPriceDao.findFirst300BySymbolOrderByAddTimeDesc(coinSymbol);
//        log.info("TRYING to get prices for " + coinSymbol + ":" + coinPriceList);
        return coinPriceList.stream().map(CoinPriceDto::toCoinPriceDto).collect(Collectors.toList());
    }

    // Get last prices of marketdate
    public List<CoinPriceDto> getLastPrices() {
//        log.info("TRYING get last prices from mao" + lastPriceMap);   // ???
        final List<CoinPrice> coinPriceList = coinPriceDao.findLastPrices();
        return coinPriceList.stream().map(CoinPriceDto::toCoinPriceDto).collect(Collectors.toList());
    }

    public List<ContactData> getSegmentData(String segmentId) {
        List<ContactData> result = new ArrayList<>(10);
        for (int i = 0; i < 10; i++) {
            result.add(new ContactData(i + "address@gmail.com"));
        }
        return result;
    }

    public List<String> getSegments() {
        List<String> result = new ArrayList<>(2);
        result.add(xy);
        result.add(ab);
        return result;
    }
//    private CoinPriceDto toCoinPriceDto(CoinPriceBase c) {
//        CoinPriceDto d = new CoinPriceDto(c.getSymbol());
//        d.setAskPrice(c.getAskPrice());
//        d.setBidPrice(c.getBidPrice());
//        d.setLastPrice(c.getLastPrice());
//        d.setVolume(c.getVolume());
//        d.setAddTime(c.getAddTime());
//        return d;
//    }
//    @ResponseStatus(HttpStatus.NOT_FOUND)
//    @ExceptionHandler(NoSuchElementException.class)
//    public String return400(NoSuchElementException ex) {
//        return ex.getMessage();
//    }
}
