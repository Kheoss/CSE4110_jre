#!/bin/bash

if [ -z "$1" ]; then
  echo "Usage: $0 <number_of_instances>"
  exit 1
fi

num_instances=$1


node server/server/index.js --peers=$1 &

