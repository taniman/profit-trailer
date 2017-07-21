package nl.komtek.gpi.controllers;

import com.cf.data.map.poloniex.PoloniexDataMapper;
import com.cf.data.model.poloniex.PoloniexChartData;
import com.cf.data.model.poloniex.PoloniexTradeHistory;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.komtek.gpi.services.GunbotProxyService;
import nl.komtek.gpi.utils.Util;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Elroy on 17-6-2017.
 */
@Controller
public class GunbotProxyController {

	@Autowired
	private GunbotProxyService gunbotProxyService;
	@Value("${doubleBuyProtection:false}")
	private boolean doubleBuyProtection;
	@Value("${doubleBuyProtectionSeconds:0}")
	private int doubleBuyProtectionSeconds;
	@Autowired
	private Util util;
	private Logger logger = LogManager.getLogger(GunbotProxyController.class);
	private PoloniexDataMapper mapper = new PoloniexDataMapper();

	@RequestMapping(value = "/public/**")
	@ResponseBody
	public String interceptAllCalls(HttpServletRequest request) {
		logger.debug("intercepted -- " + request.getRequestURL() + "?" + request.getQueryString() + "?command=" + request.getParameter("command"));
		return "intercepted";
	}

	@RequestMapping(value = "/public/**", params = "command=returnChartData")
	@ResponseBody
	public String publicRequestChartData(HttpServletRequest request,
	                                     @RequestParam String currencyPair,
	                                     @RequestParam String start,
	                                     @RequestParam long period) throws InterruptedException {

		String result = gunbotProxyService.getChartData(currencyPair, period);

		return filterChartDataByDate(start, result);
	}

	@RequestMapping(value = "/public/**", params = "command=returnTicker")
	@ResponseBody
	public String publicRequestTicker() {
		return gunbotProxyService.getTicker();
	}

	@RequestMapping(value = "/tradingApi/**")
	@ResponseBody
	public String tradingRequests(HttpServletRequest request) {
		logger.debug(request.getRequestURL() + "??command=" + request.getParameter("command"));
		request.getParameterMap().keySet().stream().forEach((e) -> System.out.print(e + "-"));
		return "trading api";
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=returnCompleteBalances")
	@ResponseBody
	public String tradingRequestCompleteBalances(HttpServletRequest request) {

		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			String key = request.getHeader("key");
			market = gunbotProxyService.getMarket(key);
		}
		return gunbotProxyService.getCompleteBalances(market);
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=returnOpenOrders")
	@ResponseBody
	public String tradingRequestOpenOrders(HttpServletRequest request,
	                                       @RequestParam String currencyPair) {
		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			String key = request.getHeader("key");
			market = gunbotProxyService.getMarket(key);
		}
		String result = gunbotProxyService.getOpenOrders(market);

		JsonElement jElement = new JsonParser().parse(result);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonArray jArray = hideOpenOrders(jObject.getAsJsonArray(currencyPair));
		return jArray != null ? jArray.toString() : "[]";
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=returnTradeHistory")
	@ResponseBody
	public String tradingRequestTradeHistory(HttpServletRequest request,
	                                         @RequestParam String currencyPair,
	                                         @RequestParam(required = false) String start) {

		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			String key = request.getHeader("key");
			market = gunbotProxyService.getMarket(key);
		}
		String result = gunbotProxyService.getTradeHistory(market);

		return filterTradeHistoryByDate(currencyPair, start, result);
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=cancelOrder")
	@ResponseBody
	public String tradingRequestCancelOrder(HttpServletRequest request,
	                                        @RequestParam String orderNumber) {

		String key = request.getHeader("key");
		return gunbotProxyService.cancelOrder(key, orderNumber);
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=sell")
	@ResponseBody
	public String tradingRequestSell(HttpServletRequest request,
	                                 @RequestParam String currencyPair,
	                                 @RequestParam BigDecimal rate,
	                                 @RequestParam BigDecimal amount) {
		String key = request.getHeader("key");
		return gunbotProxyService.sellOrder(key, currencyPair, rate, amount);
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=buy")
	@ResponseBody
	public String tradingRequestBuy(HttpServletRequest request,
	                                HttpServletResponse response,
	                                @RequestParam String currencyPair,
	                                @RequestParam BigDecimal rate,
	                                @RequestParam BigDecimal amount) throws IOException {

		boolean globalSellOnlyMode = Boolean.parseBoolean(util.getConfigurationProperty("sellOnlyMode"));
		boolean pairSellOnlyMode = Boolean.parseBoolean(util.getConfigurationProperty(String.format("%s_sellOnlyMode", currencyPair)));
		if (globalSellOnlyMode || pairSellOnlyMode) {
			JsonObject jsonObject = new JsonObject();
			String message = String.format("You are not allowed to buy. Sell Only mode is active for %s", currencyPair);
			jsonObject.addProperty("error", message);
			logger.info(jsonObject.toString());
			return jsonObject.toString();
		}

		String key = request.getHeader("key");
		if (doubleBuyProtection || doubleBuyProtectionSeconds > 0) {
			return gunbotProxyService.buyOrderWithProtection(key, currencyPair, rate, amount);
		} else {
			return gunbotProxyService.buyOrder(key, currencyPair, rate, amount);
		}
	}

	private String filterTradeHistoryByDate(String currency, String start, String result) {
		final long startLong;
		if (start.indexOf(".") > 0) {
			startLong = Long.valueOf(start.substring(0, start.indexOf(".")));
		} else {
			startLong = Long.valueOf(start);
		}

		if (result.equals("[]")) {
			return result;
		}

		JsonParser jsonParser = new JsonParser();
		JsonElement jElement = jsonParser.parse(result);
		JsonObject jObject = jElement.getAsJsonObject();
		JsonObject filteredjObject = new JsonObject();
		for (Map.Entry entry : jObject.entrySet()) {
			if (!currency.equalsIgnoreCase("all") && !currency.equalsIgnoreCase(entry.getKey().toString())) {
				continue;
			}

			List<PoloniexTradeHistory> tradeHistory = mapper.mapTradeHistory(entry.getValue().toString());
			JsonArray filteredjArray = new JsonArray();
			tradeHistory.stream()
					.filter(e -> e.date.toEpochSecond(ZoneOffset.UTC) >= startLong)
					.map(e -> jsonParser.parse(e.toString()))
					.forEach(filteredjArray::add);

			filteredjObject.add(entry.getKey().toString(), filteredjArray);
		}

		if (currency.equals("all")) {
			return (filteredjObject.entrySet().size() == 0) ? "[]" : filteredjObject.toString();
		} else {
			return (filteredjObject.entrySet().size() == 0) ? "[]" : filteredjObject.getAsJsonArray(currency).toString();
		}
	}

	private String filterChartDataByDate(String start, String result) {
		final long startLong;
		if (start.indexOf(".") > 0) {
			startLong = Long.valueOf(start.substring(0, start.indexOf(".")));
		} else {
			startLong = Long.valueOf(start);
		}

		if (result.equals("[]")) {
			return result;
		}

		if (result.contains("error")) {
			return result;
		}

		List<PoloniexChartData> chartData = mapper.mapChartData(result);

		JsonParser jsonParser = new JsonParser();
		JsonArray filteredjArray = new JsonArray();
		chartData.stream()
				.filter(e -> e.date >= startLong)
				.map(e -> jsonParser.parse(e.toString()))
				.forEach(filteredjArray::add);

		return filteredjArray.toString();
	}

	private JsonArray hideOpenOrders(JsonArray jsonArray){

		for (Iterator<JsonElement> it = jsonArray.iterator(); it.hasNext(); ) {
			JsonElement element = it.next();
			JsonObject jsonObject = element.getAsJsonObject();
			String orderNumber = jsonObject.get("orderNumber").getAsString();
			String[] orderNumbersToHide = StringUtils.trimAllWhitespace(util.getConfigurationProperty("hideOrders","")).split(",");
			if (Arrays.asList(orderNumbersToHide).contains(orderNumber)){
				it.remove();
			}
		}
		return jsonArray;
	}
}
