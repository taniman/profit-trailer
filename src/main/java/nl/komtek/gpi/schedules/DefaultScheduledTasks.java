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
@ConditionalOnProperty(name="default_apiKey", havingValue = "")
public class DefaultScheduledTasks {

	private static final String market = "default";
	private Logger logger = LogManager.getLogger(DefaultScheduledTasks.class);
	@Autowired
	private GunbotProxyService gunbotProxyService;

	@Scheduled(fixedDelay = 9000)
	public void updateCompleteBalances() {

		try {
			gunbotProxyService.getCompleteBalancesScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 1500)
	public void updateTradeHistory() {
		try {
			gunbotProxyService.getTradeHistoryScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 2000)
	public void updateOpenOrders() {
		try {
			gunbotProxyService.getOpenOrdersScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Scheduled(fixedDelay = 10000)
	public void updateBalances() {
		try {
			gunbotProxyService.getBalancesScheduled(market);
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
