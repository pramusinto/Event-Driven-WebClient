#!/bin/bash

# Add IP and hostname to /etc/hosts
#echo "10.70.135.144 dev-sas.pefindobirokredit.com" >> /etc/hosts
#echo "10.70.24.100  uat-sas.pefindobirokredit.com" >> /etc/hosts
echo "10.70.135.144  dev-sas.idscoresystem.id" >> /etc/hosts
echo "10.70.24.100   uat-sas.idscoresystem.id" >> /etc/hosts
echo "10.70.189.100 sas.idscoresystem.id" >> /etc/hosts
echo "10.50.189.100 dr-sas.idscoresystem.id" >> /etc/hosts
echo "10.70.15.31 kafka1" >> /etc/hosts
echo "10.70.15.32 kafka2" >> /etc/hosts
echo "10.70.15.33 kafka3" >> /etc/hosts

# Echo the content of the /etc/hosts file (optional, for verification purposes)
cat /etc/hosts

# Set the SPRING_PROFILES_ACTIVE environment variable
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-default}

# Run the Java command with additional parameters
java -jar /app/service.jar --spring.profiles.active=${SPRING_PROFILES_ACTIVE}