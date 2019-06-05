package elif.marketdata.api.controller;

import elif.marketdata.api.service.CoinChartDataService;
import elif.marketdata.api.service.MarketDataService;
import elif.marketdata.common.CoinPriceDto;
import elif.marketdata.common.dto.ContactData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.List;

@RestController
public class MarketDataController {
    @Autowired
    MarketDataService marketDataService;
    @Autowired
    CoinChartDataService coinChartDataService;

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

    @GetMapping(value = "/prices")
    public ResponseEntity<List<CoinPriceDto>> getCoinPrices() {
        return ResponseEntity.ok(marketDataService.getLastPrices());
    }

    @GetMapping(value = "/segmentdata/{segmentId}")
    public ResponseEntity<List<ContactData>> getSegmentData(@PathVariable String segmentId) {
        return ResponseEntity.ok(marketDataService.getSegmentData(segmentId));
    }

    @GetMapping(value = "/segments")
    public ResponseEntity<List<String>> getSegments() {
        return ResponseEntity.ok(marketDataService.getSegments());
    }

    @GetMapping(value = "/prices/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPrice(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = marketDataService.getPrice(symbol);
        return ResponseEntity.ok(coinPriceDtos);
    }

    @GetMapping(value = "/priceLive/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPriceLive(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = marketDataService.getCoinPriceLive(symbol);
        System.out.println(coinPriceDtos);
        return ResponseEntity.ok(coinPriceDtos);
    }

    @GetMapping(value = "/priceDaily/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPriceDaily(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = coinChartDataService.getCoinPriceDaily(symbol);
        return ResponseEntity.ok(coinPriceDtos);
    }

    @GetMapping(value = "/priceWeekly/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPriceWeekly(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = coinChartDataService.getCoinPriceWeekly(symbol);
        return ResponseEntity.ok(coinPriceDtos);
    }

    @GetMapping(value = "/priceMonthly/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPriceMonthly(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = coinChartDataService.getCoinPriceMonthly(symbol);
        return ResponseEntity.ok(coinPriceDtos);
    }

    @GetMapping(value = "/priceYearly/{symbol}")
    public ResponseEntity<List<CoinPriceDto>> getCoinPriceYearly(@PathVariable String symbol) {
        List<CoinPriceDto> coinPriceDtos = coinChartDataService.getCoinPriceYearly(symbol);
        return ResponseEntity.ok(coinPriceDtos);
    }
}
