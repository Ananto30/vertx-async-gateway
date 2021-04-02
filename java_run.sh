#!/usr/bin/env sh

./mvnw clean package -Dmaven.test.skip=true

#java -jar target/starter-1.0.0-SNAPSHOT-fat.jar -cluster -cluster-host 192.168.0.100 -instance 4

java -jar target/starter-1.0.0-SNAPSHOT-fat.jar -cluster -cluster-public-host 192.168.0.100 -cluster-public-port 15701 -instance 4
