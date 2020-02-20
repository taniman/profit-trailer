#!/bin/bash

### Linux .jar Update Script for ProfitTrailer
### LAST UPDATED 24 Apr 2019

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

### OPTIONS ###    ./linux-update.sh option1 option2
### This script can be executed with some optional parameters 
### auto      - automaticlaly upgrade without any confirmation to the latest version
### noupdate  - do not update the script from github
### betaurl   - Enter the url to a profitTrailer Zip file to update to that beta version without confirmation e.g https://domain.com.ProfitTrailer-2.3.2.zip

### INSTALL DEPENDENCIES ###

### Check if unzip is installed ###
if ! [ -x "$(command -v unzip)" ]; then
	echo "Unzip is not installed, Installing now..."
	sudo apt install unzip
	
	if ! [ -x "$(command -v unzip)" ]; then
		echo
		echo "$(tput setaf 1)Something went wrong.... $(tput sgr0)"
		exit
	else
		echo $(tput setaf 2)
		echo "Unzip Installed"
		echo $(tput sgr0)
	fi
fi

### Check if curl is installed ###
if ! [ -x "$(command -v curl)" ]; then
	echo "Curl is not installed, Installing now..."
	sudo apt install curl
	
	if ! [ -x "$(command -v curl)" ]; then
		echo
		echo "$(tput setaf 1)Something went wrong.... $(tput sgr0)"
		exit
	else
		echo $(tput setaf 2)
		echo "Curl Installed"
		echo $(tput sgr0)
	fi
fi

### Set all child processes to this locale language to prevent rev from breaking
export LC_ALL='en_US.UTF-8'

###Get the directory and filename of the script###
DIR=$(dirname "$(readlink -f "$0")")
script=$(basename "$0")

### Update linux-update script unless noupdate option is used###
if [[ ! "$@" == *"noupdate"* ]]; then
	### download update.sh from github. -s silent -O output to a file ###
	curl -s https://raw.githubusercontent.com/taniman/profit-trailer/master/update.sh -O > /dev/null
	### Check if the file is newer ###
	### Get line containing LAST UPDATED (line 4 typically) ###
	### -i ignore case, -m 1 match only first instance ###
	olddate=$(grep -i -m 1 'updated' "$DIR"/"$script")
	newdate=$(grep -i -m 1 'updated' update.sh)
	if [[ ! $olddate == $newdate ]]; then
		mv -f update.sh "$DIR"/"$script" 2>/dev/null
		chmod +x "$DIR"/"$script"
		if [[ ! $1 ]]; then
			exec "$DIR"/"$script"
		else
			exec "$DIR"/"$script" "$@"
		fi
	### Remove the downloaded update.sh if it is not the only copy ###
	elif [[ ! "$script" == "update.sh" ]]; then
		rm -rf update.sh
	fi
	
else
	echo
	echo "Skipped script update"
	sleep 2
	
fi
### Clear screen and print header ###
clear
echo $(tput setaf 3)
echo "##################################################"
echo "          ProfitTrailer Update Script            "
echo "##################################################"
echo $(tput sgr0)

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
		### pad the columns by 8 places for legibility %-8s\n ###
		echo "$(tput setaf 6)Your current configuration is: $(tput sgr0)"
		paste <(printf "%-8s\n" "${name[@]}") <(printf "%-8s\n" "${path[@]}")
		if [[ "$@" == *"auto"* || "$@" == *"ProfitTrailer"* ]]; then
			skipsetup=Y
		else
			echo
			read -p "Do you wish to proceed with this configuration? (Y/N) " skipsetup
		fi
	fi
fi

### SETUP ###
clear
if [[ $skipsetup == "n" ]] || [[ $skipsetup == "N" ]]; then

	### Set proceed variable to No, causing loop until user confirms proceed later ###
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

### Get latest Version number
latest=$(curl -s https://api.github.com/repos/taniman/profit-trailer/releases | grep tag_name | cut -d '"' -f 4 | sed -n '1p')
current=$(unzip -p "${path[1]}"/ProfitTrailer.jar  META-INF/MANIFEST.MF | grep Implementation-Version | cut -d ' ' -f 2)
url=$(curl -s https://api.github.com/repos/taniman/profit-trailer/releases | grep browser_download_url | cut -d '"' -f 4 | sed -n '1p')

### Break the current version into SemVer segments ###
IFS='.' read -a version_parts <<< "$current"
major=${version_parts[0]}
minor=${version_parts[1]}
### Strip Beta Version if required ###
patch=$(echo ${version_parts[2]} | cut -d '-' -f 1)

clear
echo $(tput setaf 3)
echo "##################################################"
echo "                      Update"
echo "##################################################"
echo $(tput sgr0)
echo
echo " The current version is $(tput setaf 6) $current $(tput sgr0)"
echo " Latest release is version $(tput setaf 6) $latest $(tput sgr0)"


if ! [[ "$@" == *"auto"* || "$@" == *"/ProfitTrailer-"* ]]; then
	echo
	echo "Please select an option below: (1-7)"


	### Loop through the selection menu until a valid version and url is found ###
	while [[ $exists == *"404 Not Found"* ]] || [[ -z $exists ]]; do
		
		if [[ $major == "2" ]] && [[ $minor == "2" ]] && [[ ! $patch == "12" ]]; then
			echo $(tput setaf 3)
			echo "CAUTION!"
			echo "You need to update to 2.2.12 and run the bot before updating to $latest"
			echo $(tput sgr0)
			echo "Select Choose Specific Version (5) from the options below and enter 2.2.12 when prompted"
		elif [[ $major == "2" ]] && [[ $minor == "1" ]] && [[ ! $patch == "30" ]] || [[ $major == "2" ]] && [[ $minor == "0" ]]; then
			echo $(tput setaf 3)
			echo "CAUTION!"
			echo "You need to update to 2.1.30 and run the bot before updating to $latest"
			echo $(tput sgr0)
			echo "Select Choose Specific Version (5) from the options below and enter 2.1.30 when prompted"
		fi	
		
		### Use options menu to select the version number of the desired download. Also allow Beta Url to be entered ###
		options=("Latest Release" "Increment Major Version" "Increment Minor Version" "Increment Patch Version" "Choose Specific Version" "Beta Patch" "Exit")
		select opt in "${options[@]}"
		do
			case "$opt" in
				"Latest Release")
					version=$latest
					break
					;;
				"Increment Major Version")
					newmajor=$((major + 1))
					version="$newmajor.0.0"
					break
					;;
				"Increment Minor Version")
					newminor=$((minor + 1))
					version="$major.$newminor.0"
					break
					;;
				"Increment Patch Version")
					newpatch=$((patch + 1))
					version="$major.$minor.$newpatch"
					break
					;;
				"Choose Specific Version")
					### Regex for x.x.x ###
					rx='^([0-9]+\.){2}(\*|[0-9]+)$'
					read -p "Please enter the version you wish to update to: " version
					while [[ ! $version =~ $rx ]]; do
						echo "Enter the version number in the format (x.x.x)"
						read -p "Please enter the version you wish to update to: " version
						echo
					done
					break
					;;
				"Beta Patch")
					echo 
					read -p "Enter the full url to the Beta Zip file: " download
					echo
					version=$(echo $download | rev | cut -d'/' -f 1 | rev | sed 's/\(.*\).zip/\1/' | sed 's/\(.*\).jar/\1/' | rev | sed 's/\(.*\)-reliarTtiforP/\1/' | rev)
					break
					;;
				"Exit")
					echo $(tput setaf 1)
					echo Process Aborted.... 
					echo $(tput sgr0)
					exit
					;;
			esac
		done
		
		### If the download URL was not set (Beta patch) set it now and adjust it if necessary for the selected version ###	
		if [ -z "$download" ]; then
			download=${url//$latest/$version}
		fi

		### get the http status of the url to determine if it is a real link or not ###
		exists=$(curl -Is $download | head -1)
		if [[ $exists == *"404 Not Found"* ]]; then
			download=
			echo $(tput setaf 1)
			echo "Download URL for $version does not exist or is not reachable."
			echo $(tput sgr0)
			echo "Please select an option below: (1-7)"
		fi
	done

	echo
	echo " Updating to version $(tput setaf 6) $version $(tput sgr0)"
	echo
	read -p "Do you want to continue? (Y/N) " continue
	echo

	### If something else is entered ask for a new answer ### 
	while [[ ! $continue == "y" ]] && [[ ! $continue == "Y" ]] && [[ ! $continue == "n" ]] && [[ ! $continue == "N" ]]; do
		echo Please Try Again...
		read -p "Do you want to continue? (Y/N) " continue
		echo
	done
### If auto parameter or beta url was used as an argument, skip the menu and update to the latest release ###
else
	continue=Y
	version=$latest
	for arg in "$@"; do
		if [[ $arg == *"http"* ]]; then
			download=$arg
			version=$(echo $download | rev | cut -d'/' -f 1 | rev | sed 's/\(.*\).zip/\1/' | sed 's/\(.*\).jar/\1/' | rev | sed 's/\(.*\)-reliarTtiforP/\1/' | rev)
		fi	
	done
	echo
	secs=$((10))
	while [ $secs -gt 0 ]; do
	   echo -ne " Updating to version $(tput setaf 6)$version$(tput sgr0) in $secs\033[0K seconds...\r"
	   sleep 1
	   : $((secs--))
	done
fi	

if [[ $continue == "y" ]] || [[ $continue == "Y" ]]; then
   
	### Download & extract latest version of PT ###
	echo
	echo "$(tput setaf 2) === Downloading ProfitTrailer $version === $(tput sgr0)"
	### -q for quiet to minimise output but --progress-bar to give us a progress bar ###
	curl $download -L -O --progress-bar
	echo
	echo "$(tput setaf 2) === Extracting download === $(tput sgr0)"
	### unzip the jar file only from the zip. -q for quiet, -j to prevent extracting directories ###
	unzip -q -j -o ProfitTrailer-$version.zip '*jar'
	
	### Set loc variable to No, If user ends up running this script from within a Bot folder we wont delete their jar file ###
	loc=N
	
	### Stop each BOT and Copy ProfitTrailer.jar to each instance, then restart it ###
	for ((i=1; i<=PTinstances; i++)); do

		### Get the current status of the process ####
		status=$(pm2 info "${name[$i]}" | grep 'status' | sed -n '1p' | cut -d '/' -f 2-)

				rename=$(pm2 info "$newname" | grep 'exec cwd' | sed -n '1p' | cut -d '/' -f 2-)
				### remove any leading or trailing spaces or tabs and also column bars from pm2 output by reversing and cutting ###
				nowhitespace=$(echo "$rename" | xargs | rev | cut -d ' ' -f 2- | rev )
				path[$i]=/"$nowhitespace"

		
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

		### Reload only if process was online ###
		if [[ $status = *online* ]]; then
			pm2 reload "${name[$i]}"
			echo "$(tput setaf 2) === Pausing 30 seconds while ${name[$i]} loads === $(tput sgr0)"
			sleep 30
		fi
	done
#clear			
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
