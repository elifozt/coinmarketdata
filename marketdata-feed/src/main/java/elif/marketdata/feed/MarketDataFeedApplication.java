package elif.marketdata.feed;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableAutoConfiguration
@ComponentScan({"elif.marketdata"})
@EnableJpaRepositories("elif.marketdata.common.jpa")
@EntityScan({"elif.marketdata.common.jpa"})
public class MarketDataFeedApplication {

    public static void main(String[] args) {
        SpringApplication.run(MarketDataFeedApplication.class, args);
    }
}
