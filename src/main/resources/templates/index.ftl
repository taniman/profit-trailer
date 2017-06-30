<#setting number_format=",##0.00000000">
<#assign total=0>
<!DOCTYPE html>

<html lang="en">

<body>
	Your BTC Balance = ${balance}
	<br />
	<br />
	<table border="1">
		<thead>
			<th>Market</th>
			<th>In BTC</th>
			<th>Buy/Bought</th>
			<th>Sell/Sold</th>
			<th>Last price</th>
			<th>How's the price?</th>
		<thead>
	<#list monitoringDatas as monitoringData>
		<tbody>
        <tr>
            <td>${monitoringData.market._pair}</td>

            <#if monitoringData.altcoinBalance gt 0>
                <#assign btc=monitoringData.altcoinBalance * monitoringData.lastPrice>
                <#assign total = total + btc>
                <td>${btc}</td>
            <#else>
                <td>0.00000000</td>
            </#if>

            <#if monitoringData.altcoinBalance gt 0>
                <td>${monitoringData.boughtPrice!0.0}</td>
            <#else>
               <td>${monitoringData.priceToBuy!0.0}</td>
            </#if>

            <#if monitoringData.altcoinBalance gt 0>
                <td>${monitoringData.priceToSell!0.0}</td>
            <#else>
               <td>${monitoringData.soldPrice!0.0}</td>
            </#if>

            <td>${monitoringData.lastPrice!0.0}</td>
            <td>${monitoringData.reason!""}</td>
        </tr>
        </tbody>
    </#list>
        <tr>
            <td>Total</td>
            <td>${total}</td>
        </tr>
    </table>
</body>

</html>