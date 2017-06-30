# Gunbot Proxy Community Edition
Free Proxy for Gunbot on Poloniex

# How to run
Download the released jar file or compile your own using the source code
Put the jar file in a directory
Put application.properties in the smae directory and fill in your apiKeys and secrets
Open ALLPAIRS-params.js and at the botom after module.exports = config; put the following line
process.env.NODE_TLS_REJECT_UNAUTHORIZED = "0";

To be able to run the application you need Java 8. 
Open a terminal or command prompt and type
java -jar GunbotProxyCommunity-0.9.0.jar

If you did everything correctly the application will start without giving you any error messages.
Open a web browser and go to
http://localhost:8081/public/
It should return the text. "Intercepted". 
So everything is up and running.

Now for the final step you need to edit your host file.
On windows C:\Windows\System32\drivers\etc\hosts
On linux /etc/hosts

Put the following
127.0.0.1	poloniex.com


Please use the proxy on a VPS or machine that you do not use normally. 
Once you change the host file the poloniex.com website will not work properly anymore.


Now run your bots and enjoy!
