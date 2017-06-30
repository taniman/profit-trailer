package nl.komtek.gpi.bot;

import com.cf.client.poloniex.PoloniexExchangeService;
import com.cf.data.map.poloniex.PoloniexDataMapper;
import com.cf.data.model.poloniex.PoloniexChartData;
import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;
import eu.verdelhan.ta4j.Tick;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.AverageProfitableTradesCriterion;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;
import eu.verdelhan.ta4j.indicators.simple.ClosePriceIndicator;
import eu.verdelhan.ta4j.indicators.statistics.StandardDeviationIndicator;
import eu.verdelhan.ta4j.indicators.trackers.SMAIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollinger.BollingerBandsLowerIndicator;
import eu.verdelhan.ta4j.indicators.trackers.bollinger.BollingerBandsMiddleIndicator;
import eu.verdelhan.ta4j.trading.rules.StopGainRule;
import eu.verdelhan.ta4j.trading.rules.StopLossRule;
import nl.komtek.gpi.services.GunbotProxyService;
import org.joda.time.DateTime;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by Elroy on 26-6-2017.
 */
public class IndicatorCalculators {

	private static boolean analysisMode = false;
	private static String testCurrency = null;
	private static double profitRatio = 0.60;
	private static boolean analysisModeProfitOnly = false;
	private GunbotProxyService GunbotProxyService = new GunbotProxyService();
	private final PoloniexDataMapper mapper = new PoloniexDataMapper();
	private static final PoloniexExchangeService poloniexExchangeService = new PoloniexExchangeService("", "");
	private static NumberFormat formatter = new DecimalFormat("0.000000000");

	private AnalysisCriterion profitTradesRatio = new AverageProfitableTradesCriterion();
	private TotalProfitCriterion profitCriterion = new TotalProfitCriterion();
	private AnalysisCriterion riskRewardRatio = new RiskRewardRatioCriterion();

	private Map<String, List<PoloniexChartData>> marketDataMap = new ConcurrentHashMap<>();

	public Optional<NamedStrategy> calculateBestStrategy(String currency, int startSampleDays, int totalSampleDays,
	                                                     boolean onlyShowBest) {

		Calendar start = Calendar.getInstance();
		start.add(Calendar.DATE, startSampleDays);
		start.set(Calendar.HOUR,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.SECOND,0);
		start.set(Calendar.MILLISECOND,0);
		Calendar end = Calendar.getInstance();
		end.add(Calendar.DATE, startSampleDays + totalSampleDays);
		start.set(Calendar.HOUR,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.SECOND,0);
		start.set(Calendar.MILLISECOND,0);

		TimeSeries fiveMinuteSeries = createTimeSeries(currency, start.getTimeInMillis(), end.getTimeInMillis(), 300);
		TimeSeries thirthyMinuteSeries = createTimeSeries(currency, start.getTimeInMillis(), end.getTimeInMillis(), 1800);

		//5 minutes
		ClosePriceIndicator fiveMinuteClose = new ClosePriceIndicator(fiveMinuteSeries);
		BollingerBandsLowerIndicator fiveMinuteBBLow = createBBLow(fiveMinuteSeries, fiveMinuteClose, 50);

		//30 minutes
		ClosePriceIndicator thirthyMinuteClose = new ClosePriceIndicator(thirthyMinuteSeries);
		BollingerBandsLowerIndicator thirthyMinuteBBLow = createBBLow(thirthyMinuteSeries, thirthyMinuteClose, 50);

		Rule buyingRule = new EnhancedCrossedDownIndicatorRule(fiveMinuteBBLow, fiveMinuteClose, 1)
				.and(new EnhancedCrossedDownIndicatorRule(thirthyMinuteBBLow, thirthyMinuteClose, 6));

		Map<NamedStrategy, TradingRecord> strategyAndResult = new HashMap<>();
		for (int gain = 2; gain <= 10; gain++) {
			for (int loss = 1; loss <= 10; loss++) {
				if (loss > gain) {
					continue;
				}
				Rule sellingRule = new StopGainRule(fiveMinuteClose, Decimal.valueOf(gain))
						.or(new StopLossRule(fiveMinuteClose, Decimal.valueOf(loss)));
				NamedStrategy strategy = new NamedStrategy(currency, gain, loss, buyingRule, sellingRule);
				TradingRecord tradingRecord = fiveMinuteSeries.run(strategy);
				if (!isValidStrategy(fiveMinuteSeries, tradingRecord)) {
					continue;
				}
				strategyAndResult.put(strategy, tradingRecord);
			}
		}

		NamedStrategy strategy = null;
		if (strategyAndResult.size() > 0) {
			if (analysisMode) {
				printStrategyStatistics(fiveMinuteSeries, strategyAndResult, onlyShowBest);
			}
			strategy = (NamedStrategy) profitCriterion.chooseBest(fiveMinuteSeries, new ArrayList<>(strategyAndResult.keySet()));
		}
		return Optional.ofNullable(strategy);
	}

	public double runStrategy(NamedStrategy strategy, String currency,
	                          long startInMillis, long endInMillis) throws Exception {

		TimeSeries fiveMinuteSeries = createTimeSeries(currency, startInMillis, endInMillis, 300);
		TimeSeries thirthyMinuteSeries = createTimeSeries(currency, startInMillis, endInMillis, 1800);

		//5 minutes
		ClosePriceIndicator fiveMinuteClose = new ClosePriceIndicator(fiveMinuteSeries);
		BollingerBandsLowerIndicator fiveMinuteBBLow = createBBLow(fiveMinuteSeries, fiveMinuteClose, 40);

		//30 minutes
		ClosePriceIndicator thirthyMinuteClose = new ClosePriceIndicator(thirthyMinuteSeries);
		BollingerBandsLowerIndicator thirthyMinuteBBLow = createBBLow(thirthyMinuteSeries, thirthyMinuteClose, 40);

		Rule buyingRule = new EnhancedCrossedDownIndicatorRule(fiveMinuteBBLow, fiveMinuteClose, 1)
				.and(new EnhancedCrossedDownIndicatorRule(thirthyMinuteBBLow, thirthyMinuteClose, 6));

		int gain = strategy.getGain();
		int loss = strategy.getLoss();

		Rule sellingRule = new StopGainRule(fiveMinuteClose, Decimal.valueOf(gain))
				.or(new StopLossRule(fiveMinuteClose, Decimal.valueOf(loss)));
		NamedStrategy runStrategy = new NamedStrategy(currency, gain, loss, buyingRule, sellingRule);

		Map<NamedStrategy, TradingRecord> strategyAndResult = new HashMap<>();
		TradingRecord tradingRecord = fiveMinuteSeries.run(runStrategy);

		if (tradingRecord.getTradeCount() == 0) {
			return 0;
		}

		DateTime date = new DateTime(startInMillis);
		System.out.println(date + "--" + currency + "-" + tradingRecord.getTradeCount());
		strategyAndResult.put(runStrategy, tradingRecord);
		if (1 == 2) {
			System.out.println("Running the strategy for " + currency + "......");
			double profit = printStrategyStatistics(fiveMinuteSeries, strategyAndResult, false);
			System.out.println("End run");
		}
		double profit = profitCriterion.calculate(fiveMinuteSeries, tradingRecord);

		return profit;
	}

	private TimeSeries createTimeSeries(String currency, long startInMillis, long endInMillis, long period) {

		List<PoloniexChartData> xMinuteChartData = marketDataMap.get(currency + "_" + period);
		List<Tick> ticks = new ArrayList<>();
		for (PoloniexChartData data : xMinuteChartData) {

			if ((data.date * 1000) >= startInMillis && (data.date * 1000) < endInMillis) {
				Tick tick = new Tick(
						new DateTime(data.date * 1000), data.open, data.high,
						data.low, data.close, data.volume.doubleValue());

				ticks.add(tick);
			}
		}
		return new TimeSeries(ticks);
	}

	private boolean isValidStrategy(TimeSeries series, TradingRecord tradingRecord) {
		if (tradingRecord.getTradeCount() == 0) {
			return false;
		}

		if (profitTradesRatio.calculate(series, tradingRecord) < profitRatio) {
			return false;
		}
		return true;
	}

	public void collectMarketData(List<String> markets, List<Long> periods) {
		Calendar start = Calendar.getInstance();
		start.set(Calendar.HOUR,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.SECOND,0);
		start.set(Calendar.MILLISECOND,0);
		start.add(Calendar.DATE, -165);
		Calendar end = Calendar.getInstance();
		start.set(Calendar.HOUR,0);
		start.set(Calendar.MINUTE,0);
		start.set(Calendar.SECOND,0);
		start.set(Calendar.MILLISECOND,0);
		System.out.println("Starting data collection " + end.toString());
		ExecutorService es = Executors.newCachedThreadPool();
		for (String market : markets) {
			if (!market.startsWith("USDT_")) {
				continue;
			}
			if (testCurrency != null && !market.equals(testCurrency)) {
				continue;
			}
			es.execute(() -> {
				for (Long period : periods) {
					String xMinuteChartDataRaw = GunbotProxyService.getBBChartData(market,
							start.getTimeInMillis() / 1000, end.getTimeInMillis() / 1000, period);
					List<PoloniexChartData> xMinuteChartData = mapper.mapChartData(xMinuteChartDataRaw);
					marketDataMap.put(market + "_" + period, xMinuteChartData);
				}
			});

		}
		es.shutdown();
		try {
			boolean finshed = es.awaitTermination(5, TimeUnit.MINUTES);
			System.out.println("Data collection ended " + Calendar.getInstance().toString());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	private double printStrategyStatistics(TimeSeries series,
	                                       Map<NamedStrategy, TradingRecord> strategyAndResultMap,
	                                       boolean onlyShowBest) {
		double profit = 0;

		Strategy bestProfitableTradeStrategy = profitTradesRatio.chooseBest(series, new ArrayList<>(strategyAndResultMap.keySet()));
		Strategy bestProfitStrategy = profitCriterion.chooseBest(series, new ArrayList<>(strategyAndResultMap.keySet()));

		for (Map.Entry<NamedStrategy, TradingRecord> entry : strategyAndResultMap.entrySet()) {
			NamedStrategy strategy = entry.getKey();
			TradingRecord tradingRecord = entry.getValue();

			if (onlyShowBest && (!strategy.equals(bestProfitableTradeStrategy) && !strategy.equals(bestProfitStrategy))) {
				continue;
			}

			System.out.println(String.format("-------------------%s------------------------", strategy));
			if (onlyShowBest) {
				if (strategy.equals(bestProfitableTradeStrategy)) {
					System.out.println("Best Profitable -- Number of trades for our strategy: " + tradingRecord.getTradeCount());
				} else {
					System.out.println("Best Profit -- Number of trades for our strategy: " + tradingRecord.getTradeCount());
				}
			} else {
				System.out.println("Number of trades for our strategy: " + tradingRecord.getTradeCount());
			}

			// Getting the profitable trades ratio
			System.out.println("Profitable trades ratio: " + profitTradesRatio.calculate(series, tradingRecord));
			System.out.println("Risk-reward ratio: " + riskRewardRatio.calculate(series, tradingRecord));
			for (Trade trade : tradingRecord.getTrades()) {
				Tick tick = series.getTick(trade.getEntry().getIndex());
				String endTime = tick.getEndTime().toString("hh:mm dd/MM/yyyy");
				String openValue = formatter.format(tick.getOpenPrice().toDouble());
				System.out.println(String.format("Date:%s, Open:%s", endTime, openValue));
			}
			double tmpProfit = profitCriterion.calculate(series, tradingRecord);
			System.out.println("Profit:" + tmpProfit);
			profit += tmpProfit;
		}
		//profit = profit / strategyAndResultMap.size();
		//System.out.println("Total Profit: " + profit);
		System.out.println("");
		System.out.println(String.format("---------------------------------------------------------"));

		return profit;
	}

	private BollingerBandsLowerIndicator createBBLow(TimeSeries series, ClosePriceIndicator close, int timeFrame) {
		Indicator<Decimal> SMA = new SMAIndicator(close, timeFrame);
		StandardDeviationIndicator SD = new StandardDeviationIndicator(new ClosePriceIndicator(series), timeFrame);
		BollingerBandsMiddleIndicator bb_mid = new BollingerBandsMiddleIndicator(SMA);
		BollingerBandsLowerIndicator bb_low = new BollingerBandsLowerIndicator(bb_mid, SD, Decimal.valueOf(2));

		return bb_low;
	}

	public static void main(String[] args) {
		analysisMode = false;
		//testCurrency = "BTC_BTS";
		profitRatio = 0.0;
		int totalSampleDays = 7;
		int startSampleDays = -(totalSampleDays + 1);
		IndicatorCalculators calculators = new IndicatorCalculators();
		List<String> markets = poloniexExchangeService.returnAllMarkets();
		List<Long> periods = new ArrayList<>();
		List<Double> profits = new ArrayList<>();
		periods.add(300L);
		periods.add(1800L);
		ExecutorService es;
		List<Callable<Future>> tasks = new ArrayList<>();
		calculators.collectMarketData(markets, periods);

		for (int sampleDays = 7; sampleDays <= totalSampleDays; sampleDays++) {
			profits.clear();
			for (int tradingDay = 0; tradingDay < 15; tradingDay++) {

				Calendar start = Calendar.getInstance();
				start.add(Calendar.DATE, startSampleDays + sampleDays - tradingDay);
				start.set(Calendar.HOUR,0);
				start.set(Calendar.MINUTE,0);
				start.set(Calendar.SECOND,0);
				start.set(Calendar.MILLISECOND,0);
				Calendar end = Calendar.getInstance();
				end.add(Calendar.DATE, startSampleDays + sampleDays - tradingDay + 1);
				start.set(Calendar.HOUR,0);
				start.set(Calendar.MINUTE,0);
				start.set(Calendar.SECOND,0);
				start.set(Calendar.MILLISECOND,0);
				//System.out.println(String.format("%d-%d-%d", start.get(Calendar.DATE), start.get(Calendar.MONTH) + 1, start.get(Calendar.YEAR)));
				es = Executors.newCachedThreadPool();
				tasks.clear();
				for (String market : markets) {
					if (!market.startsWith("USDT_")) {
						continue;
					}
					if (testCurrency != null && !market.equals(testCurrency)) {
						continue;
					}
					int currentTradingDay = tradingDay;
					int currentSampledays = sampleDays;

					Future future = es.submit(() -> {
						Optional<NamedStrategy> bestStrategy = calculators.calculateBestStrategy(market, startSampleDays - currentTradingDay, currentSampledays, true);

						if (bestStrategy.isPresent()) {
							double tmpProfit = 0;
							try {
								tmpProfit = calculators.runStrategy(bestStrategy.get(), market, start.getTimeInMillis(), end.getTimeInMillis());
							} catch (Exception e) {
								System.out.println(String.format("Currency %s threw an exception for tradingDay: %d and sampleDay:%d",
										market, currentTradingDay, currentSampledays));
							}

							synchronized (profits) {
								if (tmpProfit > 0) {
									profits.add(tmpProfit);
								}
							}
						}
					});
					tasks.add(() -> future);
				}
				try {

					es.invokeAll(tasks);

					es.shutdown();
					while(!es.isTerminated()){

					}
					OptionalDouble avg = profits.stream().mapToDouble(d -> d).average();
					//System.out.println(String.format("Daily:%d -- Average profit:%f on total trades %f", sampleDays, avg.orElse(0.0), profits.size()));
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			OptionalDouble avg = profits.stream().mapToDouble(d -> d).average();
			System.out.println(String.format("2 weeks:%d -- Average profit:%f  on total trades %d", sampleDays, avg.orElse(0.0), profits.size()));
		}
	}

//	// rising markets
//            if(percentChange > 0.015) {
//		baseBuyLevel = baseBuyLevel / 2;
//		baseBuyLevelIncreasePerVolume = baseBuyLevelIncreasePerVolume / 2;
//	}
//
//	// fallings markets
//            else if(percentChange < -0.015) {
//		baseBuyLevel = baseBuyLevel / 1;
//		baseBuyLevelIncreasePerVolume = baseBuyLevelIncreasePerVolume / 1;
//	}
//
//	// steady markets
//            else {
//		baseBuyLevel = baseBuyLevel / 1.5;
//		baseBuyLevelIncreasePerVolume = baseBuyLevelIncreasePerVolume / 1.5;
//	}
}
