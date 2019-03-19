package elif.marketdata.common.jpa;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "coin_price")
public class CoinPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;
    String symbol;
    BigDecimal askPrice;
    BigDecimal bidPrice;
    BigDecimal lastPrice;
    BigDecimal volume;
    long addTime = System.currentTimeMillis();

    public CoinPrice() {
    }

    public CoinPrice(String symbol) {
        this.symbol = symbol;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getAddTime() {
        return addTime;
    }

    public void setAddTime(long addTime) {
        this.addTime = addTime;
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
