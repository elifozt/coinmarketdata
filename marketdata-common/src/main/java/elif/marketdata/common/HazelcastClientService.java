package elif.marketdata.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.hazelcast.client.HazelcastClient;
import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientNetworkConfig;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;
import com.hazelcast.core.ITopic;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.util.Map;

@Component
@Lazy
public class HazelcastClientService {
    private HazelcastInstance client;   //static final yapmali miyiz?
    @Autowired
    private Environment env;

    @PostConstruct
    public void init() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setNetworkConfig(new ClientNetworkConfig().addAddress(env.getProperty("hazelcast.host")));
        client = HazelcastClient.newHazelcastClient(clientConfig);
    }

    public ITopic<String> getPriceTopic() {

        return client.getTopic("PRICE_TOPIC");      //priceTopic var olabilir mi?
    }

    public void publish(Map<String, CoinPriceDto> cpm) throws Exception{
        // convert coinPrice to a serializable obj
        CoinPriceSet cps = new CoinPriceSet();
        cpm.values().forEach(cps::addCoinPrice);

        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String jsonCps = ow.writeValueAsString(cps);
        
        // Write to hc topic
        getPriceTopic().publish(jsonCps);
    }

    public IMap<String, String> getCoinChartDataMap(){
        return client.getMap("CHART_DATA");
    }
}
