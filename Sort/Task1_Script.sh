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
    scp Slave*.class StringComparator.class pi@$line:
    nohup ssh pi@$line "java SlaveTask1" &
done < $1

sleep 2

echo "Running MasterTask1 pi code"
java -Xmx400m -Xms256m MasterTask1 $1 $2 $3
