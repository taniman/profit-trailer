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
@ConditionalOnProperty(name = "default_ETH_apiKey", havingValue = "")
public class ETHScheduledTasks {

	private Logger logger = LogManager.getLogger(ETHScheduledTasks.class);

	@Autowired
	private GunbotProxyService gunbotProxyService;

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

	@Scheduled(fixedDelay = 10000)
	public void updateBalancesETH() {
		try {
			if (gunbotProxyService.isActiveMarket("ETH")) {
				gunbotProxyService.getBalancesScheduled("ETH");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
