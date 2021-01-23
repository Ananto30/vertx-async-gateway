#!/usr/bin/env sh

./mvnw clean package -Dmaven.test.skip=true

java -jar target/starter-1.0.0-SNAPSHOT-fat.jar -cluster -cluster-host 192.168.0.100 -instance 4
