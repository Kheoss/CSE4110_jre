#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <number_of_instances>"
  exit 1
fi

num_instances=$1

for ((i=1; i<=num_instances; i++))
do
  java -jar demo-jvm/build/libs/demo-jvm-all.jar  & 
  #  ./gradlew :demo-jvm:run &
done

# RED='\033[0;31m'
# GREEN='\033[0;32m'
# NC='\033[0m' # No Color

# (java -jar demo-jvm/build/libs/demo-jvm-all.jar  ) > >(while read line; do echo -e "${RED}$line${NC}"; done) &

# (java -jar demo-jvm/build/libs/demo-jvm-all.jar  ) > >(while read line; do echo -e "${GREEN}$line${NC}"; done) &
# wait
