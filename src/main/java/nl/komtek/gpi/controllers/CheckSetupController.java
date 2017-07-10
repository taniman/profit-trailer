package nl.komtek.gpi.controllers;

import nl.komtek.gpi.services.GunbotProxyService;
import nl.komtek.gpi.utils.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Elroy on 10-7-2017.
 */
@Controller
@RequestMapping("/checkSetup/**")
public class CheckSetupController {

	@Autowired
	private GunbotProxyService gunbotProxyService;
	@Autowired
	private Util util;
	private Map<String, String> setupData = new HashMap<>();

	@RequestMapping
	public ModelAndView checkSetup(ModelMap modelMap) {
		try {
			String keyName = "default_apiKey";
			String secretName = "default_apiSecret";
			String apiKey = util.getEnvProperty(keyName);
			String apiSecret = util.getEnvProperty(secretName);
			if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(apiSecret)) {
				setupData.put(keyName, String.format("Please setup %s and %s", keyName, secretName));
			} else {
				gunbotProxyService.analyzeResult(gunbotProxyService.checkDefaultKey("default"));
				setupData.put("defaultKey", "Looking good!");
			}
		} catch (Exception e) {
			setupData.put("defaultKey", e.getMessage());
		}

		for (int i = 1; i <= 10; i++) {
			String keyName = String.format("apiKey%d", i);
			String secretName = String.format("apiSecret%d", i);
			String apiKey = util.getEnvProperty(keyName);
			String apiSecret = util.getEnvProperty(secretName);
			if (i == 1 && (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(apiSecret))) {
				setupData.put(keyName, String.format("Please setup %s and %s", keyName, secretName));
				break;
			}
			if (StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(apiSecret)) {
				break;
			}
			try {
				gunbotProxyService.analyzeResult(gunbotProxyService.checkTradingKey(apiKey));
				setupData.put(keyName, "Looking good!");
			} catch (Exception e) {
				setupData.put(keyName, e.getMessage());
			}
		}

		//check if hostfile was setup correctly
		try {
			InetAddress address = InetAddress.getByName("poloniex.com");
			if (address.getHostAddress().equals("127.0.0.1")) {
				setupData.put("hostfile", "Looking good!");
			} else {
				setupData.put("hostfile", "poloniex.com is not pointing to 127.0.0.1");
			}
		} catch (UnknownHostException e) {
			setupData.put("hostfile", e.getMessage());
		}

		try {
			InetAddress address = InetAddress.getByName("www.poloniex.com");
			if (!address.getHostAddress().equals("127.0.0.1")) {
				setupData.put("www.poloniex.com", "Looking good!");
			} else {
				setupData.put("www.poloniex.com", "www.poloniex.com should not point to 127.0.0.1");
			}
		} catch (UnknownHostException e) {
			setupData.put("www.poloniex.com", e.getMessage());
		}

		String gunbotLocation = util.getEnvProperty("gunbot.location");
		if (!StringUtils.isEmpty(gunbotLocation.length())) {
			if (!gunbotLocation.startsWith("file://")) {
				setupData.put("Gunbot location", "Your file location should start with 'file://'");
			} else {
				File file = new File(gunbotLocation);
				if (file.exists()) {
					setupData.put("Gunbot location", "Looking good!");
				} else {
					setupData.put("Gunbot location", "The specified location does not exist");
				}
			}
		} else {
			setupData.put("Gunbot location", "You cannot use monitoring without this");
		}

		modelMap.put("setupData", setupData);
		return new ModelAndView("setupData", modelMap);
	}
}
