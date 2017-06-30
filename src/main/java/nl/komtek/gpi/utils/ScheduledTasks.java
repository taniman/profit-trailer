package nl.komtek.gpi.utils;

import nl.komtek.gpi.services.GunbotProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Created by Elroy on 23-6-2017.
 */
@Component
public class ScheduledTasks {

	@Autowired
	private GunbotProxyService gunbotProxyService;

	@Scheduled(fixedDelay=10000)
	public void updateCompleteBalances() {
		gunbotProxyService.getCompleteBalancesScheduled();
	}

	@Scheduled(fixedDelay=2000)
	public void updateTicker() {
		gunbotProxyService.getTickerScheduled();
	}

	@Scheduled(fixedDelay=1500)
	public void updateTradeHistory() {
		gunbotProxyService.getTradeHistoryScheduled();
	}

	@Scheduled(fixedDelay=2000)
	public void updateOpenOrders() {
		gunbotProxyService.getOpenOrdersScheduled();
	}
}
