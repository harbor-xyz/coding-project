#!/bin/bash

echo "This script will help setup the application"

sleep 3

echo "Checking for docker..."

docker version

if [ "$?" != "0" ]
then
	echo "Docker is not found. You might need to install docker before running this script"
	echo "Check HELP.md file to run application without the docker setup"
	exit
fi

echo "Compiling the application and creating docker image locally for the application"

./gradlew bootBuildImage

echo "Docker image is created successfully"

sleep 3

echo "Checking for Docker-compose"

docker-compose version

if [ "$?" != "0" ]
then    
        echo "Docker-compose is not found. You might need to install docker-compose before running this script"
        exit
fi

docker-compose up
