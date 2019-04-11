package elif.marketdata.common;

import elif.marketdata.common.jpa.CoinPriceBase;

import java.math.BigDecimal;
import java.util.Objects;

public class CoinPriceDto {
    private String symbol;
    private BigDecimal askPrice;
    private BigDecimal bidPrice;
    private BigDecimal lastPrice;
    private BigDecimal volume;
    private long time;
    private long addTime;

    protected CoinPriceDto() {
    }

    public CoinPriceDto(CoinPriceBase cpb) {
        this.symbol = cpb.getSymbol();
        this.askPrice = cpb.getAskPrice();
        this.bidPrice = cpb.getBidPrice();
        this.lastPrice = cpb.getLastPrice();
        this.volume = cpb.getVolume();
        this.addTime = cpb.getAddTime();
    }

    public CoinPriceDto(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getAskPrice() {
        return askPrice;
    }

    public void setAskPrice(BigDecimal askPrice) {
        this.askPrice = askPrice;
    }

    public BigDecimal getBidPrice() {
        return bidPrice;
    }

    public void setBidPrice(BigDecimal bidPrice) {
        this.bidPrice = bidPrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CoinPriceDto coinPrice = (CoinPriceDto) o;
        return Objects.equals(symbol, coinPrice.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return "CoinPrice{" +
                "symbol=" + symbol +
                ", askPrice=" + askPrice +
                ", bidPrice=" + bidPrice +
                ", volume=" + volume +
                ", lastPrice=" + lastPrice +
                '}';
    }
}
