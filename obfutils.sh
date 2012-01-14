#!/bin/bash

# Replacement utils, helpful when updating the mod.
# @author Jimeo Wan
# License: MIT 
 
# Replace class names
if [[ ($1 == "-class") && $2 && $3 ]]
then
	sed -i "s/\([(< ]\)$2\([)> ]\)/\1$3\2/" src/*.java
	
else
	HELP=1
fi

# Help
if [ $help ]
then
	echo 'Usage:'
	echo 'obfutils.sh -class FROMCLASS TOCLASS'
fi

