package elif.marketdata.api.elif.marketdata.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.core.EntryEvent;
import com.hazelcast.map.impl.MapListenerAdapter;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.HazelcastClientService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class CoinChartDataService {
    @Autowired
    HazelcastClientService hc;
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
            log.info("HZ MapListener Event " + value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private List<CoinPriceDto> toList(String jSonValue) throws IOException {
        return mapper.readValue(jSonValue, new TypeReference<List<CoinPriceDto>>() {
        });
    }

    public List<CoinPriceDto> getCoinPriceDaily(String coinSymbol) {
        String key = coinSymbol + "_daily";
        List<CoinPriceDto> value = coinChartDataCache.get(key);
        if (value !=null ) {
            log.info("Getting price from local cache for coin:" + coinSymbol);
            return value;
        } else {
            final String jsonValue = hc.getCoinChartDataMap().get(key);
            if (jsonValue != null) {
                try {
                    log.info("Getting price from Hazelcast for coin:" + coinSymbol);
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
}
