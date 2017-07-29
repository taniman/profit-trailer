package nl.komtek.gpi.controllers;

import com.google.gson.Gson;
import nl.komtek.gpi.models.MonitoringData;
import nl.komtek.gpi.services.GunbotProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Elroy on 30-6-2017.
 */
@Controller
@RequestMapping("/monitoring/**")
public class MonitoringController {

	@Autowired
	private ResourceLoader resourceLoader;
	@Autowired
	private ResourcePatternResolver resourcePatternResolver;
	@Value("${gunbot.location:}")
	private String gunbotLocation;
	private Gson gson = new Gson();
	@Autowired
	private GunbotProxyService gunbotProxyService;

	@RequestMapping
	public ModelAndView monitoring(ModelMap modelMap) throws IOException, InterruptedException {
		String saveFilePatern = gunbotLocation.endsWith("/") ? gunbotLocation + "*-save.json" : gunbotLocation + "/*-save.json";
		String logFilePatern = gunbotLocation.endsWith("/") ? gunbotLocation + "*-log.txt" : gunbotLocation + "/*-log.txt";
		Resource[] saveResources = resourcePatternResolver.getResources(saveFilePatern);
		Resource[] logResources = resourcePatternResolver.getResources(logFilePatern);

		List<String> logPaths = new ArrayList<>();
		for (Resource resource : logResources) {
			logPaths.add(resource.getFilename().replace("-log.txt", ""));
		}

		List<MonitoringData> monitoringDatas = new ArrayList<>();
		for (Resource resource : saveResources) {
			String tmpName = resource.getFilename().replace("-save.json", "");
			if (!logPaths.contains(tmpName)) {
				continue;
			}
			byte[] data = Files.readAllBytes(resource.getFile().toPath());
			monitoringDatas.add(gson.fromJson(new String(data), MonitoringData.class));
		}
		modelMap.put("monitoringDatas", monitoringDatas);
		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			market = "BTC";
		}
		modelMap.put("balance", gunbotProxyService.getBTCBalance(market));
		modelMap.put("gunbotLocation", !StringUtils.isEmpty(gunbotLocation));
		return new ModelAndView("index", modelMap);
	}
}
