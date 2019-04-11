package elif.marketdata.common.jpa;

import javax.persistence.*;

@Table(name = "coin_price_2hour")
@Entity
public class CoinPrice2Hour extends CoinPriceBase {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public CoinPrice2Hour() {
    }

    public CoinPrice2Hour(String symbol) {
        super(symbol);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
