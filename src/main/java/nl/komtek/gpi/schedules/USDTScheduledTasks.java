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
@ConditionalOnProperty(name="default_USDT_apiKey", havingValue = "")
public class USDTScheduledTasks {

	private Logger logger = LogManager.getLogger(USDTScheduledTasks.class);

	@Autowired
	private GunbotProxyService gunbotProxyService;

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
	public void updateBalancesUSDT() {
		try {
			if (gunbotProxyService.isActiveMarket("USDT")) {
				gunbotProxyService.getBalancesScheduled("USDT");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
