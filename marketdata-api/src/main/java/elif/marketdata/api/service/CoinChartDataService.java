package elif.marketdata.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.impl.MapListenerAdapter;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.HazelcastClientService;
import elif.marketdata.common.jpa.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class CoinChartDataService {
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

    final private Map<String, List<CoinPriceDto>> coinChartDataCache = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(CoinChartDataService.class);

    @PostConstruct
    public void init() {
        hc.getCoinChartDataMap().addEntryListener(new MapListenerAdapter() {
            @Override
            public void entryAdded(EntryEvent event) {
                createCoinChartMapEntry(event);
            }

            @Override
            public void entryUpdated(EntryEvent event) {
                createCoinChartMapEntry(event);
            }
        }, true);
    }

    private void createCoinChartMapEntry(EntryEvent event) {
        try {
            String key = (String) event.getKey();
            String jSonValue = (String) event.getValue();
            List<CoinPriceDto> value = toList(jSonValue);
            coinChartDataCache.put(key, value);
            log.info("HZ MapListener Event " + key);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<CoinPriceDto> toList(String jSonValue) throws IOException {
        return mapper.readValue(jSonValue, new TypeReference<List<CoinPriceDto>>() {
        });
    }

    private List<CoinPriceDto> getCoinPrices(String key) {
        // Get daily prices from local cache
        List<CoinPriceDto> value = coinChartDataCache.get(key);
        if (value != null) {
            log.info("Getting prices from local cache for key:" + key);
            return value;
        } else {
            // Get daily prices from hazelcast
            final String jsonValue = hc.getCoinChartDataMap().get(key);
            if (jsonValue != null) {
                try {
                    log.info("Getting prices from Hazelcast for key:" + key);
                    value = toList(jsonValue);
                    coinChartDataCache.put(key, value);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }
        }
        return value;
    }

    // Get daily prices prices of a symbol
    public List<CoinPriceDto> getCoinPriceDaily(String symbol) {
        List<CoinPriceDto> coinPriceDaily = getCoinPrices(symbol + "_daily");
        if (coinPriceDaily == null) {
            log.info("Getting daily prices from db for " + symbol);
            List<CoinPriceBase> list = coinPrice4MinuteDao.findFirst360BySymbolOrderByAddTimeDesc(symbol);
            return list.stream().map(CoinPriceDto::toCoinPriceDto).collect(Collectors.toList());
        } else
            return coinPriceDaily;
    }

    // Get weekly prices prices of a symbol
    public List<CoinPriceDto> getCoinPriceWeekly(String symbol) {
        List<CoinPriceDto> coinPriceWeekly = getCoinPrices(symbol + "_weekly");
        if (coinPriceWeekly == null) {
            log.info("Getting weekly prices from db for " + symbol);
            List<CoinPriceBase> list = coinPriceHourDao.findFirst200BySymbolOrderByAddTimeDesc(symbol);
            return list.stream().map(CoinPriceDto::toCoinPriceDto).collect(Collectors.toList());
        } else
            return coinPriceWeekly;
    }

    // Get weekly prices prices of a symbol
    public List<CoinPriceDto> getCoinPriceMonthly(String symbol) {
        List<CoinPriceDto> coinPriceMonthly = getCoinPrices(symbol + "_monthly");
        if (coinPriceMonthly == null) {
            log.info("Getting weekly prices from db for " + symbol);
            List<CoinPriceBase> list = coinPrice2HourDao.findFirst360BySymbolOrderByAddTimeDesc(symbol);
            return list.stream().map(CoinPriceDto::toCoinPriceDto).collect(Collectors.toList());
        } else
            return coinPriceMonthly;
    }


    public List<CoinPriceDto> getCoinPriceYearly(String symbol) {
        List<CoinPriceDto> coinPriceYearly = getCoinPrices(symbol + "_yearly");
        if (coinPriceYearly == null) {
            log.info("Getting yearly prices from db for " + symbol);
            List<CoinPriceBase> list = coinPriceDayDao.findFirst360BySymbolOrderByAddTimeDesc(symbol);
            return list.stream().map(CoinPriceDto::toCoinPriceDto).collect(Collectors.toList());
        } else
            return coinPriceYearly;

    }


}