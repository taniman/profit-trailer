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
		try {
			gunbotProxyService.getTickerScheduled();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 9000)
	public void updateCompleteBalances() {

		try {
			String market = "default";
			if (gunbotProxyService.isUsingMultipleMarkets()) {
				market = "BTC";
			}
			gunbotProxyService.getCompleteBalancesScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 9200)
	public void updateCompleteBalancesETH() {
		try {
			if (gunbotProxyService.isActiveMarket("ETH")) {
				gunbotProxyService.getCompleteBalancesScheduled("ETH");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 9400)
	public void updateCompleteBalancesXMR() {
		try {
			if (gunbotProxyService.isActiveMarket("XMR")) {
				gunbotProxyService.getCompleteBalancesScheduled("XMR");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 9600)
	public void updateCompleteBalancesUSDT() {
		try {
			if (gunbotProxyService.isActiveMarket("USDT")) {
				gunbotProxyService.getCompleteBalancesScheduled("USDT");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 1500)
	public void updateTradeHistory() {
		try {
			String market = "default";
			if (gunbotProxyService.isUsingMultipleMarkets()) {
				market = "BTC";
			}
			gunbotProxyService.getTradeHistoryScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 1550)
	public void updateTradeHistoryETH() {
		try {
			if (gunbotProxyService.isActiveMarket("ETH")) {
				gunbotProxyService.getTradeHistoryScheduled("ETH");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 1600)
	public void updateTradeHistoryXMR() {
		try {
			if (gunbotProxyService.isActiveMarket("XMR")) {
				gunbotProxyService.getTradeHistoryScheduled("XMR");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 1650)
	public void updateTradeHistoryUSDT() {
		try {
			if (gunbotProxyService.isActiveMarket("USDT")) {
				gunbotProxyService.getTradeHistoryScheduled("USDT");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 2000)
	public void updateOpenOrders() {
		try {
			String market = "default";
			if (gunbotProxyService.isUsingMultipleMarkets()) {
				market = "BTC";
			}
			gunbotProxyService.getOpenOrdersScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 2050)
	public void updateOpenOrdersETH() {
		try {
			if (gunbotProxyService.isActiveMarket("ETH")) {
				gunbotProxyService.getOpenOrdersScheduled("ETH");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 2100)
	public void updateOpenOrdersXMR() {
		try {
			if (gunbotProxyService.isActiveMarket("XMR")) {
				gunbotProxyService.getOpenOrdersScheduled("XMR");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 2150)
	public void updateOpenOrdersUSDT() {
		try {
			if (gunbotProxyService.isActiveMarket("USDT")) {
				gunbotProxyService.getOpenOrdersScheduled("USDT");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 10000)
	public void updateBalances() {
		try {
			String market = "default";
			if (gunbotProxyService.isUsingMultipleMarkets()) {
				market = "BTC";
			}
			gunbotProxyService.getBalancesScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
