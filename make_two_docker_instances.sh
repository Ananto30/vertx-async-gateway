#!/usr/bin/env sh

docker rm -f vertx-async-gateway-1
docker rm -f vertx-async-gateway-2

./mvnw clean package -Dmaven.test.skip=true

docker build -t ananto30/vertx-example:vertx-async-gateway .
docker push ananto30/vertx-example:vertx-async-gateway

docker run --name vertx-async-gateway-1 -p 8888:8888 -e CLUSTER_PUBLIC_HOST=192.168.0.119 -d ananto30/vertx-example:vertx-async-gateway
docker run --name vertx-async-gateway-2 -p 8889:8888 -e CLUSTER_PUBLIC_HOST=192.168.0.100 -d vertx-async-gateway
