package nl.komtek.gpi.utils;

import nl.komtek.gpi.services.GunbotProxyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Elroy on 23-6-2017.
 */
@Component
public class ScheduledTasks {

	private Logger logger = LogManager.getLogger(ScheduledTasks.class);

	@Autowired
	private GunbotProxyService gunbotProxyService;

	@Scheduled(fixedRate = Long.MAX_VALUE)
	public void itWorks() {
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			logger.info("Running in multimarket MODE!");
		}
		logger.info("Good job, It works!");
	}

	@Scheduled(fixedDelay = 2000)
	public void updateTicker() {
		gunbotProxyService.getTickerScheduled();
	}

	@Scheduled(fixedDelay = 9000)
	public void updateCompleteBalances() {
		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			market = "BTC";
		}
		gunbotProxyService.getCompleteBalancesScheduled(market);
	}

	@Scheduled(fixedDelay = 9200)
	public void updateCompleteBalancesETH() {
		if (gunbotProxyService.isActiveMarket("ETH")) {
			gunbotProxyService.getCompleteBalancesScheduled("ETH");
		}
	}

	@Scheduled(fixedDelay = 9400)
	public void updateCompleteBalancesXMR() {
		if (gunbotProxyService.isActiveMarket("XMR")) {
			gunbotProxyService.getCompleteBalancesScheduled("XMR");
		}
	}

	@Scheduled(fixedDelay = 9600)
	public void updateCompleteBalancesUSDT() {
		if (gunbotProxyService.isActiveMarket("USDT")) {
			gunbotProxyService.getCompleteBalancesScheduled("USDT");
		}
	}

	@Scheduled(fixedDelay = 1500)
	public void updateTradeHistory() {
		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			market = "BTC";
		}
		gunbotProxyService.getTradeHistoryScheduled(market);
	}

	@Scheduled(fixedDelay = 1550)
	public void updateTradeHistoryETH() {
		if (gunbotProxyService.isActiveMarket("ETH")) {
			gunbotProxyService.getTradeHistoryScheduled("ETH");
		}
	}

	@Scheduled(fixedDelay = 1600)
	public void updateTradeHistoryXMR() {
		if (gunbotProxyService.isActiveMarket("XMR")) {
			gunbotProxyService.getTradeHistoryScheduled("XMR");
		}
	}

	@Scheduled(fixedDelay = 1650)
	public void updateTradeHistoryUSDT() {
		if (gunbotProxyService.isActiveMarket("USDT")) {
			gunbotProxyService.getTradeHistoryScheduled("USDT");
		}
	}

	@Scheduled(fixedDelay = 2000)
	public void updateOpenOrders() {
		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			market = "BTC";
		}
		gunbotProxyService.getOpenOrdersScheduled(market);
	}

	@Scheduled(fixedDelay = 2050)
	public void updateOpenOrdersETH() {
		if (gunbotProxyService.isActiveMarket("ETH")) {
			gunbotProxyService.getOpenOrdersScheduled("ETH");
		}
	}

	@Scheduled(fixedDelay = 2100)
	public void updateOpenOrdersXMR() {
		if (gunbotProxyService.isActiveMarket("XMR")) {
			gunbotProxyService.getOpenOrdersScheduled("XMR");
		}
	}

	@Scheduled(fixedDelay = 2150)
	public void updateOpenOrdersUSDT() {
		if (gunbotProxyService.isActiveMarket("USDT")) {
			gunbotProxyService.getOpenOrdersScheduled("USDT");
		}
	}

}
