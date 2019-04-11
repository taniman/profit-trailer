#!/bin/bash

### Linux .jar Update Script for ProfitTrailer
### LAST UPDATED 11 Apr 2019

### Place this script in the root folder where all your individual bot folders are and then execute it.
### For simplicity each ProfitTrailer.jar file should be nested exactly one subfolder.
### Example 
### /var
###     /opt
###         /btc bot
###         /eth bot
###         linux-update.sh
### cd to the directory you have it in e.g cd /var/opt
### execute using ./linux-update.sh to downlaod the latest github release
### execute using ./linux-update.sh https://cdn.discordapp.com/exampleurlonly/ProfitTrailer-2.0.4-B1.jar.zip to download a beta version from a link
### the link must contain the "ProfitTrailer-2.0.4-B1.jar.zip" portion to work though the ".jar" and "-B#" can be missing.

### Set all child processes to this locale language to prevent rev from breaking
export LC_ALL='en_US.UTF-8'

###Get the Directory and filename of the script###
DIR=$(dirname "$(readlink -f "$0")")
script=$(basename "$0")

### Clear screen and print header
clear
echo $(tput setaf 3)
echo "##################################################"
echo "          ProfitTrailer Update Script            "
echo "##################################################"
echo $(tput sgr0)

### INSTALL DEPENDENCIES ###

### Check if unzip is installed ###
if ! [ -x "$(command -v unzip)" ]; then
	read -p "Unzip is not installed, Do you wish to install it (Y/N)? : " install
	
	### install unzip if the user wants to proceed ###
	if [[ $install == "y" ]] || [[ $install == "Y" ]]; then
		sudo apt install unzip
		
		if ! [ -x "$(command -v unzip)" ]; then
			echo "$(tput setaf 1)Something went wrong.... $(tput sgr0)"
			echo
			exit
		else
			echo $(tput setaf 2)
			echo "Unzip Installed"
			echo $(tput sgr0)
		fi
	else
		echo "$(tput setaf 1)Process Aborted.... $(tput sgr0)"
		echo
		exit
	fi
fi

### Check if curl is installed ###
if ! [ -x "$(command -v curl)" ]; then
	read -p "Curl is not installed, Do you wish to install it (Y/N)? : " install
	
	### install curl if the user wants to proceed ###
	if [[ $install == "y" ]] || [[ $install == "Y" ]]; then
		sudo apt install curl
		
		if ! [ -x "$(command -v curl)" ]; then
			echo "$(tput setaf 1)Something went wrong.... $(tput sgr0)"
			echo
			exit
		else
			echo $(tput setaf 2)
			echo "Curl Installed"
			echo $(tput sgr0)
		fi
	else
		echo "$(tput setaf 1)Process Aborted.... $(tput sgr0)"
		echo
		exit
	fi
fi

### LOAD PREVIOUS CONFIG ###

### If no previous config can be found enter setup automatically ###
if [ ! -f "$DIR"/updatescript/name.txt ]; then
	skipsetup=N
	echo "$(tput setaf 2)No Previous Setup Found - Entering Setup $(tput sgr0)"
	echo
else
	echo Loading from saved setup...
	echo
	### load arrary of app names/ID/pid saved previously ###
	mapfile -t name < "$DIR"/updatescript/name.txt
	for i in $(seq ${#name[*]}); do
		[[ ${name[$i-1]} = $name ]]
	done
	
	### count number of instances in the arrary (exclude header by subtracting 1) ###
	PTinstances="$((${#name[@]}-1))"
	
	### load arrary of paths saved previously ###
	mapfile -t path < "$DIR"/updatescript/path.txt
	for ((i=1; i<=PTinstances; i++)); do
		[[ ${path[$i-1]} = $path ]]
		### check that the directories actually exist and an empty string is not provided ###
		if [[ -d "${path[$i]}" ]] && [[ -n "${path[$i]}" ]]; then
		continue
		else
			echo "$(tput setaf 1)Path for instance $i does no exist. Setup required. $(tput sgr0)"
			skipsetup=N
		fi
	done
	
	if [[ ! $skipsetup == "N" ]]; then
		### print out the arrays as a table for user to check ###
		### pad the columns by 8 places for legibility        ###
		echo "$(tput setaf 6)Your current configuration is: $(tput sgr0)"
		paste <(printf "%-8s\n" "${name[@]}") <(printf "%-8s\n" "${path[@]}")
		echo
		read -p "Do you wish to proceed with this configuration? (Y/N) " skipsetup
	fi
fi

### SETUP ###

if [[ $skipsetup == "n" ]] || [[ $skipsetup == "N" ]]; then

	### Set proceed variable to No, causing loop unitil user confirms proceed later ###
	proceed=N

	echo $(tput setaf 3)
	echo "##################################################"
	echo "                      Setup"
	echo "##################################################"
	echo $(tput sgr0)
	
	while [[ $proceed == "N" ]] || [[ $proceed == "n" ]]; do
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
			### use the lack of profittrailer.jar to loop through setup.
			while [[ ! -f "${path[$i]}"/ProfitTrailer.jar ]]; do
				read -p "What is the PM2 App name/ id for instance $i? " newname
				### use grep, sed and cut to find the field the pm2 process path is in. Future PM2 updates might break this and need changing ###
				rename=$(pm2 info "$newname" | grep 'exec cwd' | sed -n '1p' | cut -d '/' -f 2-)
				### remove any leading or trailing spaces or tabs and also column bars from pm2 output by reversing and cutting ###
				nowhitespace=$(echo "$rename" | xargs | rev | cut -d ' ' -f 2- | rev )
				path[$i]=/"$nowhitespace"
				
				### if user entered only a number (pm2 ID) store the app name instead.	###
				### dunno why but if you alter the below echo and then structure it literally breaks the if statement ###
				if [[ $newname =~ "^[0-9]+$" ]]
				echo
				then
					### use grep, sed and cut to find the field the pm2 process name is in. Future PM2 updates might break this and need changing ###
					rename=$(pm2 info "$newname" | grep 'name' | sed -n '1p' | cut -d ' ' -f 9-)
					### remove any leading or trailing spaces or tabs ###
					nowhitespace=$(echo "$rename" | xargs)
					name[$i]=$nowhitespace
				fi

				### If no directory exists for the given pm2 process or nothing is entered, ask again ###
				if [[ ! -d "${path[$i]}" ]] || [[ -z "${path[$i]}" ]]; then
					echo "$(tput setaf 1)Path for instance $i does not exist. Try again... $(tput sgr0)"
				### If the directory does not contain the ProfitTrailer.jar file, confirm intentions ###
				elif [[ ! -f "${path[$i]}"/ProfitTrailer.jar ]]; then
					read -p "$(tput setaf 1)Path for instance $i (${path[$i]}) does not contain ProfitTrailer.jar. Do you wish to use it anyway? (Y/N) $(tput sgr0)" empty
					if [[ $empty == "y" ]] || [[ $empty == "Y" ]] || [[ $empty == "yes" ]] || [[ $empty == "Yes" ]]; then
						break	
					fi
				fi
			done
		done
		
		### print out the arrays as a table for user to check ###
		### %-8s pads the columns to make it appear correctly ###
		echo "$(tput setaf 6)The configuration entered is: $(tput sgr0)"
		paste <(printf "%-8s\n" "${name[@]}") <(printf "%-8s\n" "${path[@]}")
		echo
		read -p "Is this correct? (Y/N) " proceed
		echo
		
		### If something else is entered ask for a new answer ### 
		while [[ ! $proceed == "y" ]] && [[ ! $proceed == "Y" ]] && [[ ! $proceed == "n" ]] && [[ ! $proceed == "N" ]]; do
			echo Please Try Again...
			read -p "Is this correct? (Y/N) " proceed
			echo
		done


		if [[ $proceed == "Y" ]] || [[ $proceed == "y" ]]; then
			### optional step to save the setup for next time ###
			read -p "Do you wish to save this setup? (Y/N) " savetofile
			
			### If something else is entered ask for a new answer ### 
			while [[ ! $savetofile == "y" ]] && [[ ! $savetofile == "Y" ]] && [[ ! $savetofile == "n" ]] && [[ ! $savetofile == "N" ]]; do
			echo Please Try Again...
			read -p "Do you wish to save this setup? (Y/N) " savetofile
			echo
			done
			
			if [[ $savetofile == "Y" ]] || [[ $savetofile == "y" ]]; then
				### create the folder for the config if necessary ###
				mkdir -p "$DIR"/updatescript
				### print each array to a file, one element per row ###
				printf "%s\n" "${name[@]}" > "$DIR"/updatescript/name.txt
				printf "%s\n" "${path[@]}" > "$DIR"/updatescript/path.txt
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

elif [[ ! $skipsetup == "y" ]] && [[ ! $skipsetup == "Y" ]]; then
	echo $(tput setaf 1)
	echo Process Aborted.... 
	echo $(tput sgr0)
	exit
fi

### DOWNLOAD AND INSTALL ###


### passing a Beta URL as a variable will use the url itself to determine version###
### the only difference is it will strip out the -Bx from the jar file name
### e.g ./update.sh https://cdn.discordapp.com/attachments/400383734777511936/443897522956533760/ProfitTrailer-2.0.4-B1.jar.zip

### extract the version number from the file name ###
version=$(echo $1 | rev | cut -d'/' -f 1 | rev | sed 's/\(.*\).zip/\1/' | sed 's/\(.*\).jar/\1/' | rev | sed 's/\(.*\)-reliarTtiforP/\1/' | rev)
download=$1

### If no variable is passed search on github ###
if [[ ! $1 ]]; then
### Find Latest Version of PT and its download url ###
version=$(curl -s https://api.github.com/repos/taniman/profit-trailer/releases | grep tag_name | cut -d '"' -f 4 | sed -n '1p')
download=$(curl -s https://api.github.com/repos/taniman/profit-trailer/releases | grep browser_download_url | cut -d '"' -f 4 | sed -n '1p')
fi

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
while [[ ! $continue == "y" ]] && [[ ! $continue == "Y" ]] && [[ ! $continue == "n" ]] && [[ ! $continue == "N" ]]; do
	echo Please Try Again...
	read -p "Do you want to continue? (Y/N) " continue
	echo
done

if [[ $continue == "y" ]] || [[ $continue == "Y" ]]; then
   
	### Download & extract latest version of PT ###
	echo
	echo "$(tput setaf 2) === Downloading ProfitTrailer $version === $(tput sgr0)"
	### -q for quiet to minimise output but --show-progress to give us a progress bar ###
	curl $download -L -O --progress-bar
	echo
	echo "$(tput setaf 2) === Extracting download === $(tput sgr0)"
	### unzip the jar file only from the zip. -q for quiet, -j to prevent extracting directories ###
	unzip -q -j -o ProfitTrailer-$version.zip '*jar' '*sh'
	### Update linux-update script ###
	mv -f linux-update.sh "$DIR"/"$script" 2>/dev/null
	chmod +x "$DIR"/"$script"
	
	### Set loc variable to No, If user ends up running this script from within a Bot folder we wont delete their jar file ###
	loc=N
	
	### Stop each BOT and Copy ProfitTrailer.jar to each instance, then restart it ###
	for ((i=1; i<=PTinstances; i++)); do
		
		### If the user is running this script inside their bots folder we avoid deleting the jar during cleanup ###
		if [[ $DIR == "${path[$i]}" ]]; then
			loc=Y
		fi
	
		echo
		echo "$(tput setaf 2) === Stopping ${name[$i]} === $(tput sgr0)"
		pm2 stop "${name[$i]}"
		echo
		echo "$(tput setaf 2) === Replacing jar file === $(tput sgr0)"
		cp ProfitTrailer.jar "${path[$i]}"
		mkdir -p "$DIR"/updatescript/"$(date +%m%d_%H%M)"/"${name[$i]}"
		cp "${path[$i]}"/data/ptdb.db "$DIR"/updatescript/"$(date +%m%d_%H%M)"/"${name[$i]}"/ptdb.db
		echo
		echo "$(tput setaf 2) === Restarting ${name[$i]} === $(tput sgr0)"
		pm2 reload "${name[$i]}"
	done
			
	### Remove downloaded Files ###
	echo
	echo "$(tput setaf 2) === Cleaning up === $(tput sgr0)"
	
	if [[ $loc == "Y" ]]; then
		rm -rf ProfitTrailer-$version*.zip
	else
		rm -rf ProfitTrailer-$version*.zip ProfitTrailer.jar
	fi
	
	### Remove all but the 5 most recent backups ###
	if [ "$(ls -ld "$DIR"/updatescript/* | wc -l)" -gt 5 ]
	then
		ls -dt "$DIR"/updatescript/*/ | tail -n +6 | xargs -d "\n" rm -r
		echo "Removed old backups from updatescript folder."
	fi

	echo $(tput setaf 3)
	echo "##################################################"
	echo "     Finished updating to ProfitTrailer $version"
	echo "##################################################"
	echo $(tput sgr0)
	
	### Present a summary of pm2 at the end ###
	pm2 status
	echo       
else
	echo $(tput setaf 1)
	echo Process Aborted.... 
	echo $(tput sgr0)
	exit
fi
