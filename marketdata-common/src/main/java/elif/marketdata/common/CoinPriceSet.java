package elif.marketdata.common;

import java.util.HashSet;
import java.util.Set;

public class CoinPriceSet  {
    private Set<CoinPriceDto> coinPrices;

    public CoinPriceSet() {
    }

    public CoinPriceSet(Set<CoinPriceDto> coinPrices) {
        this.coinPrices = coinPrices;
    }

    public void addCoinPrice(CoinPriceDto coinPrice) {
        if (coinPrices == null) {
            coinPrices = new HashSet<>();
        }
        coinPrices.add(coinPrice);
    }



    public Set<CoinPriceDto> getCoinPrices() {
        return coinPrices;
    }

    public void setCoinPrices(Set<CoinPriceDto> coinPrices) {
        this.coinPrices = coinPrices;
    }

    @Override
    public String toString() {
        return "CoinPriceSet{" +
                "coinPrices=" + coinPrices +
                '}';
    }
}
