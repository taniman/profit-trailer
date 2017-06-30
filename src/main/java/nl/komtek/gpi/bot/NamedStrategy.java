package nl.komtek.gpi.bot;

import eu.verdelhan.ta4j.Rule;
import eu.verdelhan.ta4j.Strategy;

/**
 * Created by Elroy on 28-6-2017.
 */
public class NamedStrategy extends Strategy {

	private String name;
	private int gain;
	private int loss;

	public NamedStrategy(String name, int gain, int loss, Rule entryRule, Rule exitRule) {
		super(entryRule, exitRule);
		this.name = name;
		this.gain = gain;
		this.loss = loss;
	}

	public String getName() {
		return name;
	}

	public int getGain() {
		return gain;
	}

	public int getLoss() {
		return loss;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("NamedStrategy{");
		sb.append("name='").append(name).append('\'');
		sb.append(", gain=").append(gain);
		sb.append(", loss=").append(loss);
		sb.append('}');
		return sb.toString();
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		NamedStrategy strategy = (NamedStrategy) o;

		if (gain != strategy.gain) {
			return false;
		}
		if (loss != strategy.loss) {
			return false;
		}
		return name.equals(strategy.name);
	}

	@Override
	public int hashCode() {
		int result = name.hashCode();
		result = 31 * result + gain;
		result = 31 * result + loss;
		return result;
	}
}
