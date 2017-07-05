package nl.komtek.gpi.controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import nl.komtek.gpi.services.GunbotProxyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.math.BigDecimal;

/**
 * Created by Elroy on 17-6-2017.
 */
@Controller
public class GunbotProxyController {

	@Autowired
	private GunbotProxyService gunbotProxyService;
	@Value("${doubleBuyProtection:false}")
	private boolean doubleBuyProtection;

	@RequestMapping(value = "/public/**")
	@ResponseBody
	public String interceptAllCalls(HttpServletRequest request) {
		System.out.println("intercepted -- " + request.getRequestURL() + "?" + request.getQueryString() + "?command=" + request.getParameter("command"));
		return "intercepted";
	}

	@RequestMapping(value = "/public/**", params = "command=returnChartData")
	@ResponseBody
	public String publicRequestChartData(HttpServletRequest request,
	                                     @RequestParam String currencyPair,
	                                     @RequestParam String start,
	                                     @RequestParam long period) throws InterruptedException {

		return gunbotProxyService.getChartData(currencyPair, start, period);
	}

	@RequestMapping(value = "/public/**", params = "command=returnTicker")
	@ResponseBody
	public String publicRequestTicker() {
		return gunbotProxyService.getTicker();
	}

	@RequestMapping(value = "/tradingApi/**")
	@ResponseBody
	public String tradingRequests(HttpServletRequest request) {
		System.out.println(request.getRequestURL() + "??command=" + request.getParameter("command"));
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

		JsonElement jelement = new JsonParser().parse(result);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray jarray = jobject.getAsJsonArray(currencyPair);
		return jarray != null ? jarray.toString() : "[]";
	}

	@RequestMapping(value = "/tradingApi/**", params = "command=returnTradeHistory")
	@ResponseBody
	public String tradingRequestTradeHistory(HttpServletRequest request,
	                                         @RequestParam String currencyPair) {

		String market = "default";
		if (gunbotProxyService.isUsingMultipleMarkets()) {
			String key = request.getHeader("key");
			market = gunbotProxyService.getMarket(key);
		}
		String result = gunbotProxyService.getTradeHistory(market);

		if (result.equals("[]")){
			return result;
		}
		JsonElement jelement = new JsonParser().parse(result);
		JsonObject jobject = jelement.getAsJsonObject();
		JsonArray jarray = jobject.getAsJsonArray(currencyPair);
		return jarray != null ? jarray.toString() : "[]";

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
	                                @RequestParam String currencyPair,
	                                @RequestParam BigDecimal rate,
	                                @RequestParam BigDecimal amount) {

		String key = request.getHeader("key");
		if (doubleBuyProtection) {
			return gunbotProxyService.buyOrderWithProtection(key, currencyPair, rate, amount);
		} else {
			return gunbotProxyService.buyOrder(key, currencyPair, rate, amount);
		}
	}
}
