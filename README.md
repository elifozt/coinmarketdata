Please see the running applcation by visiting : http://www.elifozturk.me/

# coinmarketdata
This is a simple cryptocurrency market data application. It shows the current and historical (daily, monthly, yearly) USD prices for popular coins such as Bitcoin, Ethereum and Litecoin.
There are 3 modules.

    1) marketdata-common: Shared classes such as JPA entities, repositories, Hazelcast Client and utilities.
    2) marketdata-feed: SpringBoot app. Market data is fetched from Binance public API and stored in the
       MySQL database every 10 seconds.
    3) marketdata-api: SpringBoot app. Http API and Websocket server that Angular app communicates with.

Hazelcast is used to broadcast live and historical price data to API services so that API services can locally cache the coin prices.
MySQL server is used to store historical coin prices.
Websocket is used to push real time coin prices to the frontend.

To build:

  $ sh build.sh

  It runs 'mvn clean compile package -DskipTests' to create the spring boot jars.

To run marketdata-feed:

  $ java -jar marketdata-feed/target/marketdata-feed-0.0.1-SNAPSHOT.jar

To run marketdata-api:

  $ java -jar marketdata-api/target/marketdata-api-0.0.1-SNAPSHOT.jar

To Do:

  1) Write JUnit tests
  2) Add last prices to the live charts using websocket
  3) Work with design

Screenshots
 
![CoinMarket Data](https://raw.githubusercontent.com/elifozt/coinmarketdata-angular/master/src/assets/img/coinprices.png)
![BTC Last Hour Price Chart](https://raw.githubusercontent.com/elifozt/coinmarketdata-angular/master/src/assets/img/BTCChart.png)