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
@ConditionalOnProperty(name="default_XMR_apiKey", havingValue = "")
public class XMRScheduledTasks {

	private Logger logger = LogManager.getLogger(XMRScheduledTasks.class);

	@Autowired
	private GunbotProxyService gunbotProxyService;

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

	@Scheduled(fixedDelay = 10000)
	public void updateBalancesXMR() {
		try {
			if (gunbotProxyService.isActiveMarket("XMR")) {
				gunbotProxyService.getBalancesScheduled("XMR");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
