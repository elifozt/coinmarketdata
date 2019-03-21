package elif.marketdata.api.controller;

import elif.marketdata.api.elif.marketdata.service.MarketDataService;
import elif.marketdata.common.CoinPriceDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
public class MarketDataController {
    @Autowired
    MarketDataService marketDataService;

    @PostConstruct
    public void init() {
        System.out.println(">>>>>>>>>>>>");
    }

    @GetMapping(value = "/hello/{name}")
    public ResponseEntity<?> sayHello(@PathVariable String name) {
        return ResponseEntity.ok("Hello " + name.toUpperCase());
    }

    @PostMapping(value = "/echo")
    public ResponseEntity<?> echo(@RequestBody String content) {
        return ResponseEntity.ok("I got:" + content);
    }

    @RequestMapping(value = "/error", method = RequestMethod.GET)
    @ResponseBody
    public String error() {
        return "Error happened";
    }

    @GetMapping(value = "/price/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPrice(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = marketDataService.getPrice(symbol);
        return ResponseEntity.ok(coinPriceDtos);
    }

    @GetMapping(value = "/prices")
    public ResponseEntity<List<CoinPriceDto>> getCoinPrices() {
        return new ResponseEntity(marketDataService.getLastPrices(), HttpStatus.OK);
    }
}
