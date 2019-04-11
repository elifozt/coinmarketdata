package elif.marketdata.common.jpa;

import javax.persistence.*;

@Table ( name = "coin_price_day")
@Entity
public class CoinPriceDay extends CoinPriceBase {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CoinPriceDay() {
        super();
    }
    public CoinPriceDay(String symbol) {
        super(symbol);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
