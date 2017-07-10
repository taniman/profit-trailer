package nl.komtek.gpi.schedules;

import nl.komtek.gpi.services.GunbotProxyService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Elroy on 23-6-2017.
 */
@Component
@ConditionalOnProperty(name="default_BTC_apiKey", havingValue = "")
public class BTCScheduledTasks {

	private static final String market = "BTC";
	private Logger logger = LogManager.getLogger(BTCScheduledTasks.class);

	@Autowired
	private GunbotProxyService gunbotProxyService;

	@Scheduled(fixedDelay = 9000)
	public void updateCompleteBalances() {

		try {
			if (gunbotProxyService.isActiveMarket(market)) {
				gunbotProxyService.getCompleteBalancesScheduled(market);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 1500)
	public void updateTradeHistory() {
		try {
			if (gunbotProxyService.isActiveMarket(market)) {
				gunbotProxyService.getTradeHistoryScheduled(market);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}


	@Scheduled(fixedDelay = 2000)
	public void updateOpenOrders() {
		try {
			if (gunbotProxyService.isActiveMarket(market)) {
				gunbotProxyService.getOpenOrdersScheduled(market);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 10000)
	public void updateBalancesBTC() {
		try {
			if (gunbotProxyService.isActiveMarket(market)) {
				gunbotProxyService.getBalancesScheduled(market);
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
