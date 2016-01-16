#!/bin/bash

# Script to initate code
# @author1 : Ruturaj Hagawane
# @author2 : FNU Shivangi
#--------------------------------------------------
# Reads host file which contains IP's of all slaves
# Connects to Slave
# Sends code to slave via scp
# Runs code
#--------------------------------------------------



# Iterate over the hosts
while IFS='' read -r line; do
    echo "Connecting to pi: $line"
    scp Slave*.class Task2OutChunk.class pi@$line:
    nohup ssh pi@$line "java SlaveTask2" &
done < $1

sleep 2

echo "Running MasterTask2 pi code"
java -Xmx400m -Xms256m MasterTask2 $1 $2 $3
