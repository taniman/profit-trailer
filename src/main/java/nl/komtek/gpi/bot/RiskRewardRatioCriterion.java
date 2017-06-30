package nl.komtek.gpi.bot;

import eu.verdelhan.ta4j.AnalysisCriterion;
import eu.verdelhan.ta4j.TimeSeries;
import eu.verdelhan.ta4j.Trade;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.analysis.criteria.AbstractAnalysisCriterion;
import eu.verdelhan.ta4j.analysis.criteria.MaximumDrawdownCriterion;
import eu.verdelhan.ta4j.analysis.criteria.TotalProfitCriterion;

/**
 * Risk reward ratio criterion.
 * <p>
 * (i.e. {@link MaximumDrawdownCriterion maximum drawdown} the  over the {@link TotalProfitCriterion total profit}.
 */
public class RiskRewardRatioCriterion extends AbstractAnalysisCriterion {

	private AnalysisCriterion totalProfit = new TotalProfitCriterion();

	private AnalysisCriterion maxDrawdown = new MaximumDrawdownCriterion();

	@Override
	public double calculate(TimeSeries series, TradingRecord tradingRecord) {
		return maxDrawdown.calculate(series, tradingRecord) / totalProfit.calculate(series, tradingRecord);
	}

	@Override
	public boolean betterThan(double criterionValue1, double criterionValue2) {
		return criterionValue1 < criterionValue2;
	}

	@Override
	public double calculate(TimeSeries series, Trade trade) {
		return maxDrawdown.calculate(series, trade) / totalProfit.calculate(series, trade);
	}

}
