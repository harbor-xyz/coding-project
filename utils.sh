#!/bin/bash


function print_line() {
	if [ "$1" == "" ]
	then
		cols=$(tput cols)
		for ((i=0; i<cols; i++));do printf "-"; done; echo
	else
		cols=$(tput cols)
		for ((i=0; i<cols; i++));do printf "$1"; done; echo
	fi
}

function log() {
	echo $1
}

function await_action() {
	echo ""
	read  -n 1 -p "Press enter to continue..." discard
}

function check_error() {
	if [ "$?" != "0" ]
	then
        	echo "Something went wrong"
		exit
		return -1
	fi
	return 0
}
