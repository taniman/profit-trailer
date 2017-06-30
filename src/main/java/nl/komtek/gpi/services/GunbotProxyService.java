package nl.komtek.gpi.services;

import com.cf.PriceDataAPIClient;
import com.cf.client.poloniex.PoloniexPublicAPIClient;
import com.cf.client.poloniex.PoloniexTradingAPIClient;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.jodah.failsafe.Failsafe;
import net.jodah.failsafe.RetryPolicy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elroy on 20-6-2017.
 */
@Component
public class GunbotProxyService {

	private Logger logger = LogManager.getLogger(GunbotProxyService.class);
	@Autowired
	private ApplicationContext applicationContext;
	@Autowired
	Environment environment;
	private PoloniexTradingAPIClient defaultClient;
	private Map<String, PoloniexTradingAPIClient> poloniexTradingAPIClients = new HashMap<>();
	private PriceDataAPIClient publicClient;
	private RetryPolicy retryPolicy;

	public GunbotProxyService() {
		publicClient = new PoloniexPublicAPIClient();
	}

	@PostConstruct
	private void initService() {
		String apiKey = environment.getProperty("default_apiKey");
		String apiSecret = environment.getProperty("default_apiSecret");
		if (apiKey == null || apiSecret == null) {
			logger.error("The default apiKey and Secret is required");
			SpringApplication.exit(applicationContext);
			return;
		}
		defaultClient = new PoloniexTradingAPIClient(apiKey, apiSecret);

		for (int i = 1; i <= 100; i++) {
			apiKey = environment.getProperty(String.format("apiKey%d", i));
			apiSecret = environment.getProperty(String.format("apiSecret%d", i));
			if (apiKey == null || apiSecret == null) {
				break;
			}
			poloniexTradingAPIClients.put(apiKey, new PoloniexTradingAPIClient(apiKey, apiSecret));
		}

		if (poloniexTradingAPIClients.size() == 0) {
			logger.error("Please setup at least 1 other apiKey for a better experience");
			SpringApplication.exit(applicationContext);
			return;
		}

		publicClient = new PoloniexPublicAPIClient();

		retryPolicy = new RetryPolicy()
				.retryOn(failure -> failure instanceof Exception)
				.withDelay(300, TimeUnit.MILLISECONDS)
				.withMaxRetries(10);
	}

	//@Cacheable(value = "BBChartData", key = "#currencyPair")
	public String getBBChartData(String currencyPair, long start, long end, long period) {

		logger.debug("BB ChartData: " + currencyPair + " -- start:" + start + " -- period:" + period);

		while (true) {
			try {
				String result = publicClient.getChartData(currencyPair, period, start, end);
				return analyzeResult(result);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	@Cacheable(value = "chartData", key = "#currencyPair")
	public String getChartData(String currencyPair, String start, long period) {

		logger.debug("chartData: " + currencyPair + " -- start:" + start + " -- period:" + period);
		long startLong = 0;
		if (start.indexOf(".") > 0) {
			startLong = Long.valueOf(start.substring(0, start.indexOf(".")));
		} else {
			startLong = Long.valueOf(start);
		}

		while (true) {
			try {
				String result = publicClient.getChartData(currencyPair, period, startLong);
				return analyzeResult(result);
			} catch (Exception e) {
				handleException(e);
			}
		}
	}

	@Cacheable("ticker")
	public String getTicker() {
		return getTickerScheduled();
	}

	@CachePut("ticker")
	public String getTickerScheduled() {
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(publicClient.returnTicker()));
		logger.debug("ticker: " + result);
		return result;
	}

	@Cacheable(value = "tradeHistory", sync = true)
	public String getTradeHistory() {
		return getTradeHistoryScheduled();
	}

	@CachePut(value = "tradeHistory")
	public String getTradeHistoryScheduled() {
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient("default");
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.returnTradeHistory("ALL")));
		logger.debug("trade history: " + result);
		return result;
	}

	@Cacheable(value = "completeBalances", sync = true)
	public String getCompleteBalances() {
		return getCompleteBalancesScheduled();
	}

	public double getBTCBalance() {
		String result = getBalancesScheduled();
		JsonElement jsonElement = new JsonParser().parse(result);
		JsonObject jsonObject = jsonElement.getAsJsonObject();
		return jsonObject.get("BTC").getAsDouble();
	}

	@CachePut(value = "completeBalances")
	public String getCompleteBalancesScheduled() {
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient("default");
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.returnCompleteBalances()));
		logger.debug("complete balances" + result);
		return result;
	}

	@CachePut(value = "completeBalances")
	public String getBalancesScheduled() {
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient("default");
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.returnBalances()));
		logger.debug("balances" + result);
		return result;
	}

	@Cacheable(value = "openOrders", sync = true)
	public String getOpenOrders() {
		return getOpenOrdersScheduled();
	}

	@CachePut(value = "openOrders")
	public String getOpenOrdersScheduled() {
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient("default");
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.returnOpenOrders("ALL")));
		logger.debug("open orders: " + result);
		return result;
	}

	@Caching(evict = {@CacheEvict(value = "openOrders"), @CacheEvict(value = "completeBalances")})
	public String cancelOrder(String orderNumber) {
		logger.debug("Canceling an order");
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient("apiKey1");
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.cancelOrder(orderNumber)));

		return result;
	}

	@Caching(evict = {@CacheEvict(value = "openOrders"), @CacheEvict(value = "completeBalances")})
	public synchronized String buyOrder(String currencyPair, BigDecimal buyPrice, BigDecimal amount) {
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient(currencyPair);
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.buy(currencyPair, buyPrice, amount, false, false, false)));
		logger.debug("Buy order" + result);
		return result;
	}

	@Caching(evict = {@CacheEvict(value = "openOrders"), @CacheEvict(value = "completeBalances")})
	public synchronized String sellOrder(String currencyPair, BigDecimal buyPrice, BigDecimal amount) {
		PoloniexTradingAPIClient tradingAPIClient = getTradingClient(currencyPair);
		String result = Failsafe.with(retryPolicy)
				.onFailedAttempt(this::handleException)
				.get(() -> analyzeResult(tradingAPIClient.sell(currencyPair, buyPrice, amount, false, true, false)));
		logger.info("Sell order" + result);
		return result;
	}

	private PoloniexTradingAPIClient getTradingClient(String currencyPair) {

		if (currencyPair.equals("default")) {
			return defaultClient;
		} else {
			String apiKey = environment.getProperty(currencyPair + "_apiKey");
			if (apiKey != null) {
				apiKey = environment.getProperty("default_apiKey");
				return poloniexTradingAPIClients.get(apiKey);
			} else {
				int random = (int) (Math.random() * 1) + poloniexTradingAPIClients.values().size() - 1;
				return (PoloniexTradingAPIClient) poloniexTradingAPIClients.values().toArray()[random];
			}
		}
	}

	private String analyzeResult(String result) {
		if (result != null && result.contains("Nonce")) {
			throw new RuntimeException("nonce error: " + result);
		} else if (result == null) {
			throw new RuntimeException("No value was returned");
		}
		return result;
	}

	private void handleException(Throwable e) {
		if (!(e instanceof RuntimeException)) {
			logger.error(e.toString());
		} else if (e instanceof NullPointerException) {
			logger.error("Something is really wrong", e);
		} else {
			logger.debug(e.toString());
		}
	}

}
