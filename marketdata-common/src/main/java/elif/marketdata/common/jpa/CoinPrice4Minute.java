package elif.marketdata.common.jpa;

import javax.persistence.*;

@Entity
@Table(name = "coin_price_minute")
public class CoinPrice4Minute extends CoinPriceBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CoinPrice4Minute() {
    }

    public CoinPrice4Minute(String symbol) {
        super(symbol);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
