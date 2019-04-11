package elif.marketdata.common.jpa;

import javax.persistence.MappedSuperclass;
import java.math.BigDecimal;

@MappedSuperclass
public class CoinPriceBase {
    private String symbol;
    private BigDecimal askPrice;
    private BigDecimal bidPrice;
    private BigDecimal lastPrice;
    private BigDecimal volume;
    private long time;
    private long addTime = System.currentTimeMillis();

    public CoinPriceBase() {
    }

    public CoinPriceBase(String symbol) {
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

    public BigDecimal getLastPrice() {
        return lastPrice;
    }

    public void setLastPrice(BigDecimal lastPrice) {
        this.lastPrice = lastPrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
    }

    @Override
    public String toString() {
        return "CoinPriceBase{" +
                "symbol='" + symbol + '\'' +
                ", askPrice=" + askPrice +
                ", bidPrice=" + bidPrice +
                ", lastPrice=" + lastPrice +
                ", volume=" + volume +
                ", time=" + time +
                ", addTime=" + addTime +
                '}';
    }
}
