echo off
set sp=--server.port=443
set sks=--server.ssl.key-store=classpath:poloniex.keystore
set sksp=--server.ssl.key-store-password=poloniex
set skp=--server.ssl.key-password=poloniex
set gbl=--gunbot.location=file://C:/Users/Taniman/Documents/GUNBOT_v3.3.2

REM set lnl=--logging.level.nl.komtek=DEBUG

REM This apiKey pair will do all the heavy lifting
set da=--default_apiKey=first api key
set ds=--default_apiSecret=your secret

REM This apiKey will do the buying and selling and orders canceling stuff. You can add more if you have lots of pairs buying and selling
set a=--apiKey1=another apiKey
set as=--apiSecret1=your secret

set dbp=--doubleBuyProtectionSeconds=60

java -jar GunbotProxyCommunity-0.9.7.jar %sp% %sks% %sksp% %skp% %da% %ds% %a% %as% %lnl% %dbp%
pause
