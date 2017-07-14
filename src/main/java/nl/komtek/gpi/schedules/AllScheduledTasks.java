package nl.komtek.gpi.schedules;

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
public class AllScheduledTasks {

	private Logger logger = LogManager.getLogger(AllScheduledTasks.class);

	@Autowired
	private GunbotProxyService gunbotProxyService;


	@Scheduled(fixedDelay = 2000)
	public void updateTicker() {
		try {
			gunbotProxyService.getTickerScheduled();
		} catch (Exception e) {
			logger.error(e);
		}
	}
}
