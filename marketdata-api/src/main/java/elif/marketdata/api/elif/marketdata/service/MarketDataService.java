package elif.marketdata.api.elif.marketdata.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.Message;
import com.hazelcast.core.MessageListener;
import elif.marketdata.api.MarketDataApplication;
import elif.marketdata.api.websocket.WebSocketHandler;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.CoinPriceSet;
import elif.marketdata.common.HazelcastClientService;
import elif.marketdata.common.jpa.CoinPrice;
import elif.marketdata.common.jpa.CoinPriceDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.annotation.PostConstruct;
import java.util.*;
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

    private static final Logger log = LoggerFactory.getLogger(MarketDataApplication.class);

    // is this thread-safe? yes.. concurrenthashmap + we only put new CoinPrice objects
    private final Map<String, CoinPriceDto> coinPriceMap = new ConcurrentHashMap<>(10);
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
                coinPriceMap.put(coinPrice.getSymbol(), coinPrice);
            });
            final String arrayStr = mapper.writer().writeValueAsString(cps.getCoinPrices());
            // create websocket json message
            webSocketHandler.sendToEveryone(arrayStr);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Get all prices of a symbol
    public List<CoinPriceDto> getPrice(String coinSymbol) {
        List<CoinPrice> coinPricePage = coinPriceDao.findFirst100BySymbolOrderByAddTimeDesc(coinSymbol);
        log.info("TRYING to get prices for " + coinSymbol + ":" + coinPricePage);
        List<CoinPriceDto> coinPriceDtoList = coinPricePage
                .stream()
                .map(coinPrice -> toCoinPriceDto(coinPrice))
                .collect(Collectors.toList());
        return coinPriceDtoList;
    }

    // Get last prices of marketdate
    public Collection<CoinPriceDto> getLastPrices() {
        log.info("TRYING get last prices from mao" + coinPriceMap);
        final List<CoinPrice> coinPriceList = coinPriceDao.findLastPrices();
        return coinPriceList.stream().map(coinPrice -> toCoinPriceDto(coinPrice)).collect(Collectors.toList());
    }

    private CoinPriceDto toCoinPriceDto(CoinPrice c) {
        CoinPriceDto d = new CoinPriceDto(c.getSymbol());
        d.setAskPrice(c.getAskPrice());
        d.setBidPrice(c.getBidPrice());
        d.setLastPrice(c.getLastPrice());
        d.setVolume(c.getVolume());
        d.setAddTime(c.getAddTime());
        return d;
    }

    /**
     * Exception handler if NoSuchElementException is thrown in this Controller
     *
     * @param ex
     * @return Error message String.
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public String return400(NoSuchElementException ex) {
        return ex.getMessage();
    }
}
