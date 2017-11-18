# ProfitTrailer
We trail the trends!

# Join the telegram group
If you have questions after reading the readme
### Main Channel
https://t.me/joinchat/FWYlMkKK-mkrSuj836ehug

|Channel Language   | Channel link                                  |
|-------------------|-----------------------------------------------|
|German             | https://t.me/joinchat/FDBA_g1yD6Iw-k46ysvYzA  |
|Portuguese         | https://t.me/joinchat/profittrailer_br        |
|Russian            | https://t.me/joinchat/DreUUw-n-aGoi2LunoJTFA  |
|Korean             | https://t.me/joinchat/DqRTeBHA8KY5_vwKBj3JzA  |
|Spanish            | https://t.me/joinchat/FmiiRhEiz1VMEfaf1ch4Hg  |
|Dutch              | https://t.me/profittrailernl                  |



# Check out the wiki
https://github.com/taniman/profit-trailer/wiki/ProfitTrailer  

# How to run
Download the latest released jar file  
https://github.com/taniman/profit-trailer/releases  

1. Make sure you have java 8 installed on your system
2. Unzip ProfitTrailer.zip file  
3. Fill your apiKeys and secrets in application.properties  
5. Open a terminal or command prompt and type  
6. CD to the ProfitTrailer directory
7. java -jar ProfitTrailer.jar -XX:+UseConcMarkSweepGC -Xmx256m -Xms256m  
   * On OSX or some linux versions you might need to sudo su and then run the java -jar command.  
8. If you did everything correctly the application will start without giving you any error messages  

### On windows
Open a web browser and go to  
http://localhost:8081/checkSetup/

### On a Linux VPS
curl http://localhost:8081/checkSetupLinux/  

All rows should say 'Looking good!'  
If this is not the case, than you have an error in your application.properties  


# Running in the background
Use the provided pm2 json file.<br />
If you have pm2 installed just use this command.<br />
pm2 start pm2-ProfitTrailer.json <br />
pm2 save <br />
This will make sure that pm2 will automatically start the bot when pm2 reloads. <br />
<br />
To see the log you could do. <br />
pm2 log 'id' <-- this is the id pm2 gave your bot

# Setup telegram so the bot can send you telegram messages
1. Talk to @BotFather   
1.1. NOT to be confused with TheBotFather channel!!!  
1.2 Once there type "/newbot" to create a bot. Answer the questions by typing answers and pressing enter.  

2. Start a chat to your bot's name (for ex. @MyTPBot) and press start button

3. Start another chat with https://telegram.me/get_id_bot This bot will reveal your Chat ID

4. Edit application.properties and set
    - telegram.botToken = "your bot token"
    - telegram.chatId = "your chat id"
    - restart the bot

5. Go to http://localhost:8081/settings/telegramTestMessage to test your telegram setup. You will receive a message if the setup is correct.

# Authorized resellers on telegram
  All resellers speak English :)
  - @Anderson_drsub (Portuguese)
  - @Moondust2010 (German)
  - @AkaDeCaoS (Spanish)
  - @T1M3C (Russian)
  - @Another_Diesel (US)  
  - @billycoin (Chinese)  
  - @freebits (Korean)
  - @djnaffie (Dutch)
  - @lowprofiler (Danish & Norwegian)
  - @gcan9 (Swedish & Greek)
  - @CryptoGuruOmigus (Japanese) 
  
  No one responding?
  - @Elroy (DEV)

