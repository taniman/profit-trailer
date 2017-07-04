# Gunbot Proxy Community Edition
Free Proxy for Gunbot on Poloniex
<br />
# Join the telegram group
If you have questions after reading the readme
https://t.me/joinchat/FWYlMg51hhEH06az2d_58g
<br />
# How to run
Download the latest released jar file or compile your own using the source code 
https://github.com/taniman/gunbotproxycommunity/releases
<br />
Put the jar file in a directory <br />
Put application.properties in the same directory and fill in your apiKeys and secrets <br />
Open ALLPAIRS-params.js and at the botom after module.exports = config; put the following line <br />
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";<br />
<br />
To be able to run the application you need Java 8 <br />
Open a terminal or command prompt and type <br />
java -jar GunbotProxyCommunity-x.x.x.jar  (replace x.x.x with your actual version number)<br />
On OSX or some linux versions you might need to sudo su and then run the java- jar command.
<br />
If you did everything correctly the application will start without giving you any error messages.<br />
Open a web browser and go to <br />
http://localhost:8081/public/ <br />
It should return the text. "Intercepted". <br /> 
So everything is up and running. <br />
<br />
Now for the final step you need to edit your host file. <br />
On windows C:\Windows\System32\drivers\etc\hosts <br />
On linux /etc/hosts <br />
<br />
Put the following <br />
127.0.0.1	poloniex.com <br />
<br />
<br />
# Warning
Please use the proxy on a VPS or machine that you do not use normally. 
Once you change the host file the poloniex.com website will not work properly anymore.


Now run your bots and enjoy!

<br />
<br />

# Donations
If you would like to send me a donation, here are my cryptocurrency addresses:

BTC: 1BGe9qmFUj3BrpjQKRASxVFHEnBeLoCNX1

ETH: 0x3ad4048c71afeebeb88dad665be6cbdfcb63c526

LTC: LTfW6rp29GepiLPvC1JSfgZNcxuxCmrsW7
