package nl.komtek.gpi.models;

/**
 * Created by Elroy on 30-6-2017.
 */
public class MonitoringData {

	private Market market;
	private Double altcoinBalance;
	private Double lastPrice;
	private Double boughtPrice;
	private Double priceToBuy;
	private Double priceToSell;
	private Double soldPrice;
	private String reason;

	public MonitoringData(Market market, Double altcoinBalance, Double lastPrice, Double boughtPrice, Double priceToBuy, Double soldPrice) {
		this.market = market;
		this.altcoinBalance = altcoinBalance;
		this.lastPrice = lastPrice;
		this.boughtPrice = boughtPrice;
		this.priceToBuy = priceToBuy;
		this.soldPrice = soldPrice;
	}

	public Market getMarket() {
		return market;
	}

	public void setMarket(Market market) {
		this.market = market;
	}

	public Double getAltcoinBalance() {
		return altcoinBalance;
	}

	public void setAltcoinBalance(Double altcoinBalance) {
		this.altcoinBalance = altcoinBalance;
	}

	public Double getLastPrice() {
		return lastPrice;
	}

	public void setLastPrice(Double lastPrice) {
		this.lastPrice = lastPrice;
	}

	public Double getBoughtPrice() {
		return boughtPrice;
	}

	public void setBoughtPrice(Double boughtPrice) {
		this.boughtPrice = boughtPrice;
	}

	public Double getPriceToBuy() {
		return priceToBuy;
	}

	public void setPriceToBuy(Double priceToBuy) {
		this.priceToBuy = priceToBuy;
	}

	public Double getSoldPrice() {
		return soldPrice;
	}

	public void setSoldPrice(Double soldPrice) {
		this.soldPrice = soldPrice;
	}

	public Double getPriceToSell() {
		return priceToSell;
	}

	public void setPriceToSell(Double priceToSell) {
		this.priceToSell = priceToSell;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
