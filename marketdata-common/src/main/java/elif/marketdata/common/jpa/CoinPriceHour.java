package elif.marketdata.common.jpa;

import javax.persistence.*;

@Table(name = "coin_price_hour")
@Entity
public class CoinPriceHour extends CoinPriceBase {
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Id
    long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CoinPriceHour() {
    }

    public CoinPriceHour(String symbol) {
        super(symbol);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}