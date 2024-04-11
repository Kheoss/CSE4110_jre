#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <number_of_instances>"
  exit 1
fi
# Set up a process group
trap 'cleanup' SIGINT SIGTERM
cleanup() {
    echo "Stopping all processes in the process group..."
    killall -9 java

}

num_instances=$1

for ((i=1; i<=num_instances; i++))
do
  #  ./gradlew :demo-jvm:run -Dorg.slf4j.simpleLogger.defaultLogLevel=error &
  # discarded output
  setsid java -jar KubernetsTest/demo-jvm-all.jar >/dev/null 2>&1 &
done

wait
