package nl.komtek.gpi.application;

import net.sf.ehcache.Cache;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * Created by Elroy on 29-6-2017.
 */
@SpringBootApplication
@Configuration
@EnableScheduling
@EnableAsync
@EnableCaching
@ComponentScan(basePackages = "nl.komtek.gpi")
public class Application {

	@Value("${server.additionalPort:8081}")
	private int additionalPort;
	@Value("${server.address:0.0.0.0}")
	private String serverAddress;
	@Value("${doubleBuyProtectionSeconds:0}")
	private int doubleBuyProtectionSeconds;
	@Value("${doubleBuyProtection:false}")
	private boolean doubleBuyProtection;

	public static void main(String[] args) {
		Map<String, Object> props = new HashMap<>();
		props.put("server.port", 443);
		props.put("server.ssl.key-store", "classpath:poloniex.keystore");
		props.put("server.ssl.key-store-password", "poloniex");
		props.put("server.ssl.key-password", "poloniex");
		new SpringApplicationBuilder()
				.sources(Application.class)
				.properties(props)
				.run(args);
	}

	@Bean
	public CacheManager cacheManager() {
		net.sf.ehcache.CacheManager cacheManager = ehCacheCacheManager().getObject();
		Cache tickerCache = new Cache("ticker", 500, false, true, 0, 0);
		Cache tradeHistoryCache = new Cache("tradeHistory", 500, false, true, 0, 0);
		Cache openOrdersCache = new Cache("openOrders", 500, false, true, 0, 0);
		Cache completeBalancesCache = new Cache("completeBalances", 5, false, true, 0, 0);
		Cache chartDataCache = new Cache("chartData", 500, false, false, 30, 30);
		Cache balancesCache = new Cache("balances", 5, false, true, 0, 0);
		Cache orderBookCache = new Cache("orderBook", 100, true, true, 0, 0);
		Cache publicTradeHistoryCache = new Cache("publicTradeHistory", 500, false, true, 0, 0);

		cacheManager.addCache(tickerCache);
		cacheManager.addCache(tradeHistoryCache);
		cacheManager.addCache(openOrdersCache);
		cacheManager.addCache(completeBalancesCache);
		cacheManager.addCache(chartDataCache);
		cacheManager.addCache(balancesCache);
		cacheManager.addCache(orderBookCache);
		cacheManager.addCache(publicTradeHistoryCache);

		if (doubleBuyProtectionSeconds > 0) {
			Cache buyOrderProtectionCache = new Cache("buyOrderProtection", 100, false, false, doubleBuyProtectionSeconds, doubleBuyProtectionSeconds);
			cacheManager.addCache(buyOrderProtectionCache);
		} else if (doubleBuyProtection) {
			Cache buyOrderProtectionCache = new Cache("buyOrderProtection", 100, false, false, 30, 30);
			cacheManager.addCache(buyOrderProtectionCache);
		}

		return new EhCacheCacheManager(cacheManager);

	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		EhCacheManagerFactoryBean factory = new EhCacheManagerFactoryBean();
		factory.setShared(true);
		return factory;
	}

	@Bean
	public TaskScheduler taskScheduler() {
		return new ConcurrentTaskScheduler(Executors.newScheduledThreadPool(4));
	}

	@Bean
	public EmbeddedServletContainerFactory servletContainer() {
		TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory();
		tomcat.addAdditionalTomcatConnectors(additionalConnector());

		return tomcat;
	}

	private Connector additionalConnector() {
		Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
		connector.setScheme("http");
		connector.setPort(additionalPort);
		if (!serverAddress.equals("0.0.0.0")) {
			connector.setAttribute("address", serverAddress);
		}
		return connector;
	}
}
