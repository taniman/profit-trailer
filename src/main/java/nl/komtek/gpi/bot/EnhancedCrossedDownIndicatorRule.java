package nl.komtek.gpi.bot;

import eu.verdelhan.ta4j.Decimal;
import eu.verdelhan.ta4j.Indicator;
import eu.verdelhan.ta4j.TradingRecord;
import eu.verdelhan.ta4j.trading.rules.CrossedDownIndicatorRule;

/**
 * Created by Elroy on 27-6-2017.
 */
public class EnhancedCrossedDownIndicatorRule extends CrossedDownIndicatorRule {

	private Integer overrideIndex;
	public EnhancedCrossedDownIndicatorRule(Indicator<Decimal> indicator, Decimal threshold, Integer overrideIndex) {
		super(indicator, threshold);
		this.overrideIndex = overrideIndex;
	}

	public EnhancedCrossedDownIndicatorRule(Indicator<Decimal> first, Indicator<Decimal> second, Integer overrideIndex) {
		super(first, second);
		this.overrideIndex = overrideIndex;
	}

	@Override
	public boolean isSatisfied(int index, TradingRecord tradingRecord) {
		int newIndex = index;
		if (overrideIndex != null){
			newIndex = index / overrideIndex;
		}
		return super.isSatisfied(newIndex, tradingRecord);
	}
}
