# ProxyBot
Enhanced Proxy for Gunbot on Poloniex <BR />
Price: 0.015 BTC  
<br />
# Join the telegram group
If you have questions after reading the readme
### Main Channel
https://t.me/joinchat/FWYlMkKK-mkrSuj836ehug

### German Channel
https://t.me/joinchat/FDBA_g1yD6Iw-k46ysvYzA

# Check out the wiki
https://github.com/taniman/proxy-bot/wiki/ProxyBot  

# Another guide
<a href="https://gunthy.org/index.php?topic=570.msg3080#msg3080">Diesel's Guide</a>

# How to run
Download the latest released jar file<br />
https://github.com/taniman/proxy-bot/releases <br />
<br />
Put the jar file in a directory <br />
Put application.properties in the same directory and fill in your apiKeys and secrets <br />
<br />
To be able to run the application you need Java 8 <br />
Open a terminal or command prompt and type <br />
java -jar ProxyBot.jar -XX:+UseConcMarkSweepGC -Xmx256m -Xms256m<br />
On OSX or some linux versions you might need to sudo su and then run the java -jar command.<br />
<br />
If you did everything correctly the application will start without giving you any error messages.

### On windows
Open a web browser and go to <br />
http://localhost:8081/checkSetup/

### On a Linux VPS
curl http://localhost:8081/checkSetupLinux/ <br />
<br />
All rows except hostfile should say 'Looking good!'<br />
If this is not the case, than you have an error in your application.properties <br />
So everything is up and running. <br />
<br />
Now for the final step you need to edit your host file. <br />
On windows C:\Windows\System32\drivers\etc\hosts <br />
On linux /etc/hosts <br />
<br />
Put the following <br />
127.0.0.1	poloniex.com <br />
and save your host file.<br />

# Final test
Open a web browser again and again go to : <br />
http://localhost:8081/checkSetup/ <br />
All rows should say 'Looking good!' <br />
If this is not the case, than you have an error in your application.properties <br />

# Running in the background
Use the provided pm2 json file.<br />
If you have pm2 installed just use this command.<br />
pm2 start pm2-GunbotProxyCommunity.json <br />
pm2 save <br />
This will make sure that pm2 will automatically start the proxy when pm2 reloads. <br />
<br />
To see the proxy log you could do. <br />
pm2 log 'id' <-- this is the id pm2 gave your proxy

# Setup telegram so the proxy can send you telegram messages
1. Talk to @BotFather   
1.1. NOT to be confused with TheBotFather channel!!!  
1.2 Once there type "/newbot" to create a bot. Answer the questions by typing answers and pressing enter.  

2. Start a chat to your bot's name (for ex. @MyProxyBot) and press start button

3. Start another chat with https://telegram.me/get_id_bot This bot will reveal your Chat ID

4. Edit application.properties and set
    - telegram.botToken = "your bot token"
    - telegram.chatId = "your chat id"

5. Go to http://localhost:8081/settings/telegramTestMessage to test your telegram setup. You will receive a message if the setup is correct.

# Invalid License
  - Talk to @Elroy on telegram to activate

# Warning
Please use the proxy on a VPS or machine that you do not use normally. <br />
Once you change the host file the poloniex.com website will not work properly anymore. <br />
<br />

