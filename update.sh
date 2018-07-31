#!/bin/bash

### Linux .jar Update Script for ProfitTrailer
### LAST UPDATED 23 JULY 2018

### Place this script in the root folder where all your individual bot folders are and then execute it.
### For simplicity each ProfitTrailer.jar file should be nested exactly one subfolder.
### Example 
### /var
###     /opt
###         /btc bot
###         /eth bot
###         update.sh
### When prompted for the directory you can then jsut type "btc bot" or "eth bot" for example.
### cd to the directory you have it in e.g cd /var/opt
### execute using ./update.sh to downlaod the latest github release
### execute using ./update.sh https://cdn.discordapp.com/exampleurlonly/ProfitTrailer-2.0.4-B1.jar.zip to download a beta version from a link
### the link must contain the "ProfitTrailer-2.0.4-B1.jar.zip" portion to work though the ".jar" and "-B#" can be missing.

### Clear screen and print header
clear
echo $(tput setaf 3)
echo "##################################################"
echo "          ProfitTrailer Update Script            "
echo "##################################################"
echo $(tput sgr0)

### Check if unzip is installed ###
if ! [ -x "$(command -v unzip)" ]; then
	read -p "Unzip is not installed, Do you wish to install it (Y/N)? : " install
	
	### install unzip if the user wants to proceed ###
	if [[ "$install" == "y" ]] || [[ "$install" == "Y" ]]; then
		sudo apt install unzip
	else
		echo "$(tput setaf 1)Process Aborted.... $(tput sgr0)"
		echo
		exit
	fi
fi

### If no previous config can be found enter setup automatically ###
if [ ! -f updatescript/name.txt ]; then
	skipsetup=N
	echo "$(tput setaf 2)No Previous Setup Found - Entering Setup $(tput sgr0)"
	echo
else
	echo Loading from saved setup...
	echo
	### load arrary of app names/ID/pid saved previously ###
	mapfile -t name < updatescript/name.txt
	for i in $(seq ${#name[*]}); do
		[[ ${name[$i-1]} = $name ]]
	done
	
	### count number of instances in the arrary (exclude header by subtracting 1) ###
	PTinstances="$((${#name[@]}-1))"
	
	### load arrary of paths saved previously ###
	mapfile -t path < updatescript/path.txt
	for ((i=1; i<=PTinstances; i++)); do
		[[ ${path[$i-1]} = $path ]]
		### check that the directories actually exist and an empty string is not provided ###
		if [[ -d "${path[$i]}" ]] && [[ -n "${path[$i]}" ]]; then
		continue
		else
			echo "$(tput setaf 1)Path for instance $i does no exist. Setup required. $(tput sgr0)"
			echo
			skipsetup=N
		fi
	done
	
	if [[ ! "$skipsetup" == "N" ]]; then
		### print out the arrays as a table for user to check ###
		### pad the columns by 8 places for legibility        ###
		echo "$(tput setaf 6)Your current configuration is: $(tput sgr0)"
		paste <(printf "%-8s\n" "${name[@]}") <(printf "%-8s\n" "${path[@]}")
		echo
		read -p "Do you wish to proceed with this configuration? (Y/N) " skipsetup
	fi
fi


if [[ "$skipsetup" == "n" ]] || [[ "$skipsetup" == "N" ]]; then

	### Set proceed variable to No, causing loop unitil user confirms proceed later ###
	proceed=N

	echo $(tput setaf 3)
	echo "##################################################"
	echo "                      Setup"
	echo "##################################################"
	echo $(tput sgr0)
	echo
    
	
	while [[ "$proceed" == "N" ]] || [[ "$proceed" == "n" ]]; do
		### wipe arrays clean then set the header for each array ###
		name=()
		path=()
		name[0]=Name/ID
		path[0]=Path
		
		### show user their current pm2 instances to assist with setup ###
		pm2 list
		echo
		read -p "How many ProfitTrailer Bots do you want to update? " PTinstances
		echo
		### ask user for the name and path of each instance they wish to update ###
		for ((i=1; i<=$PTinstances; i++)); do
			read -p "What is the PM2 App name/ id or pid for instance $i? " name[$i]
			chars="a-zA-Z0-9_-\/"
			re="\/[$chars]+"
			execpath=$(pm2 info "${name[$i]}" | grep 'exec cwd');
			if [[ $execpath =~ $re ]];
			then
				path[$i]=$BASH_REMATCH
			fi

			### If the directory provided doesnt exist or nothing is entered, ask again ###
			while [[ ! -f "${path[$i]}"/ProfitTrailer.jar ]]; do
				if [[ ! -d "${path[$i]}" ]] || [[ -z "${path[$i]}" ]]; then
					echo "$(tput setaf 1)Path for instance $i does not exist. Try again... $(tput sgr0)"
					read -p "What is the Directory for ProfitTrailer instance #$i? " path[$i]	
				else 
					### If the directory does not contain the ProfitTrailer.jar file, ask again ###
					echo "$(tput setaf 1)Path for instance $i does not contain ProfitTrailer.jar. Try Again... $(tput sgr0)"
					read -p "What is the Directory for ProfitTrailer instance #$i? " path[$i]
				fi
			done
		done
		
		### print out the arrays as a table for user to check ###
		### %-8s pads the columns to make it appear correctly ###
        echo
		echo "$(tput setaf 6)The configuration entered is: $(tput sgr0)"
		paste <(printf "%-8s\n" "${name[@]}") <(printf "%-8s\n" "${path[@]}")
		echo
		read -p "Is this correct? (Y/N) " proceed
		echo
		
		### If something else is entered ask for a new answer ### 
		while [[ ! "$proceed" == "y" ]] && [[ ! "$proceed" == "Y" ]] && [[ ! "$proceed" == "n" ]] && [[ ! "$proceed" == "N" ]]; do
			echo Please Try Again...
			read -p "Is this correct? (Y/N) " proceed
			echo
		done


		if [[ "$proceed" == "Y" ]] || [[ "$proceed" == "y" ]]; then
			### optional step to save the setup for next time ###
			read -p "Do you wish to save this setup? (Y/N) " savetofile
			
			### If something else is entered ask for a new answer ### 
			while [[ ! "$savetofile" == "y" ]] && [[ ! "$savetofile" == "Y" ]] && [[ ! "$savetofile" == "n" ]] && [[ ! "$savetofile" == "N" ]]; do
			echo Please Try Again...
			read -p "Do you wish to save this setup? (Y/N) " savetofile
			echo
			done
			
			if [[ "$savetofile" == "Y" ]] || [[ "$savetofile" == "y" ]]; then
				### create the folder for the config is necessary ###
				mkdir -p updatescript
				### print each array to a file, one element per row ###
				printf "%s\n" "${name[@]}" > updatescript/name.txt
				printf "%s\n" "${path[@]}" > updatescript/path.txt
				echo $(tput setaf 2)
				echo Configuration Saved
				echo $(tput sgr0)
			else
				echo $(tput setaf 2)
				echo Configuration Not Saved
				echo $(tput sgr0)
			fi
		else
			echo $(tput setaf 3)
			echo Starting setup again... 
			echo $(tput sgr0)
		fi
	done

elif [[ ! "$skipsetup" == "y" ]] && [[ ! "$skipsetup" == "Y" ]]; then
	echo $(tput setaf 1)
	echo Process Aborted.... 
	echo $(tput sgr0)
	exit
fi

if [[ ! $1 ]]; then
	### Find Latest Version of PT and its download url ###
	version=$(curl -s https://api.github.com/repos/taniman/profit-trailer/releases | grep tag_name | cut -d '"' -f 4 | sed -n '1p')
	download=$(curl -s https://api.github.com/repos/taniman/profit-trailer/releases | grep browser_download_url | cut -d '"' -f 4 | sed -n '1p')
	echo $(tput setaf 3)
	echo "##################################################"
	echo "                      Update"
	echo "##################################################"
	echo $(tput sgr0)
	echo "Latest release is version $(tput setaf 6) $version $(tput sgr0)"
	echo
	read -p "Do you want to continue? (Y/N) " continue
	echo

	### If something else is entered ask for a new answer ### 
	while [[ ! "$continue" == "y" ]] && [[ ! "$continue" == "Y" ]] && [[ ! "$continue" == "n" ]] && [[ ! "$continue" == "N" ]]; do
		echo Please Try Again...
		read -p "Do you want to continue? (Y/N) " continue
		echo
	done
	
    if [[ "$continue" == "y" ]] || [[ "$continue" == "Y" ]]; then
       
        ### Download & extract latest version of PT ###
		echo
		echo "$(tput setaf 2) === Downloading ProfitTrailer $version === $(tput sgr0)"
		### -q for quiet to minimise output but --show-progress to give us a progress bar ###
		curl $download -L -O --progress-bar
        echo
		echo "$(tput setaf 2) === Extracting download === $(tput sgr0)"
		### unzip the jar file only from the zip. -q for quiet, -j to prevent extracting directories ###
		unzip -q -j ProfitTrailer-$version.zip '*jar'

		### Stop each BOT and Copy ProfitTrailer.jar to each instance, then restart it ###
		for ((i=1; i<=PTinstances; i++)); do
			echo
			echo "$(tput setaf 2) === Stopping ${name[$i]} === $(tput sgr0)"
			pm2 stop "${name[$i]}"
			echo
			echo "$(tput setaf 2) === Replacing jar file === $(tput sgr0)"
			cp ProfitTrailer.jar "${path[$i]}"
			mkdir -p updatescript/"$(date +%m%d_%H%M)"/"${name[$i]}"
			cp "${path[$i]}"/ProfitTrailerData.json updatescript/"$(date +%m%d_%H%M)"/"${name[$i]}"/ProfitTrailerData.json
			echo
			echo "$(tput setaf 2) === Restarting ${name[$i]} === $(tput sgr0)"
			pm2 reload "${name[$i]}"
		done

		### Remove downloaded Files ###
		echo
		echo "$(tput setaf 2) === Cleaning up === $(tput sgr0)"
		rm -rf ProfitTrailer-$version.zip ProfitTrailer.jar
		echo $(tput setaf 3)
		echo "##################################################"
		echo "     Finished updating to ProfitTrailer $version"
		echo "##################################################"
		echo $(tput sgr0)
        
		### Present summary of pm2 at the end ###
		pm2 status
		echo       
	else
		echo $(tput setaf 1)
		echo Process Aborted.... 
		echo $(tput sgr0)
		exit
	fi
fi

### passing a Beta URL as a variable will initiate this installation process instead###
### the only difference is it will strip out the -B1 from the jar file name
### e.g ./update.sh https://cdn.discordapp.com/attachments/400383734777511936/443897522956533760/ProfitTrailer-2.0.4-B1.jar.zip
if [ -n "${1}" ]; then

	### extract the version number from the file name ###
	version=$(echo $1 | rev | cut -d'/' -f 1 | rev | sed 's/\(.*\).zip/\1/' | sed 's/\(.*\).jar/\1/' | rev | sed 's/\(.*\)-reliarTtiforP/\1/' | rev)

	echo
	echo "This Release is version $(tput setaf 6) $version $(tput sgr0)"
	read -p "Do you want to continue? (Y/N) " continue
	if [[ "$continue" == "y" ]] || [[ "$continue" == "Y" ]]; then

		### Download & extract latest version of PT ###
		echo
		echo "$(tput setaf 2) === Downloading ProfitTrailer $version === $(tput sgr0)"
		### -q for quiet to minimise output but --show-progress to give us a progress bar ###
		curl $1  -L -O --progress-bar
		echo
		echo "$(tput setaf 2) === Extracting download === $(tput sgr0)"
		### unzip the jar file only from the zip. -q for quiet, -j to prevent extracting directories ###
		unzip -q -j ProfitTrailer-$version*.zip '*jar'

		### Copy ProfitTrailer.jar to each instance ###
		for ((i=1; i<=PTinstances; i++)); do
			echo
			echo "$(tput setaf 2) === Stopping ${name[$i]} === $(tput sgr0)"
			pm2 stop "${name[$i]}"
			echo
			echo "$(tput setaf 2) === Replacing jar file === $(tput sgr0)"
			cp ProfitTrailer*.jar "${path[$i]}"/ProfitTrailer.jar
			mkdir -p updatescript/"$(date +%m%d_%H%M)"/"${name[$i]}"
			cp "${path[$i]}"/ProfitTrailerData.json updatescript/"$(date +%m%d_%H%M)"/"${name[$i]}"/ProfitTrailerData.json
			echo
			echo "$(tput setaf 2) === Restarting ${name[$i]} === $(tput sgr0)"
			pm2 reload "${name[$i]}"
			echo
		done

		### Remove downloaded Files ###
		echo "$(tput setaf 2) === Cleaning up === $(tput sgr0)"
		rm -rf ProfitTrailer-$version*.zip ProfitTrailer*.jar
		echo $(tput setaf 3)
		echo "##################################################"
		echo "   Finished updating to ProfitTrailer $version"
		echo "##################################################"
		echo $(tput sgr0)
       
		### Present summary of pm2 at the end ###
		pm2 status
		echo
	else
		echo $(tput setaf 1)
		echo Process Aborted.... 
		echo $(tput sgr0)
		exit	
	fi
fi
