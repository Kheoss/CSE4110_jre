#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <number_of_instances>"
  exit 1
fi

num_instances=$1

for ((i=1; i<=num_instances; i++))
do
   ./gradlew :demo-jvm:run &
done

wait
