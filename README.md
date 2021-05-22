# Vertx Async to Sync

[![Language grade: Java](https://img.shields.io/lgtm/grade/java/g/Ananto30/vertx-async-gateway.svg?logo=lgtm&logoWidth=18)](https://lgtm.com/projects/g/Ananto30/vertx-async-gateway/context:java)

Example of converting (legacy) async service to sync.

## Run for dev
Directly run the DevApp. All the tweaks can be done with the code.

## Run for production/testing
Can be run using the fat jar.

Create jar:
```
./mvnw clean package
```
Or skip tests:
```
./mvnw clean package -Dmaven.test.skip=true
```
____________________
Run with java:
```
java -jar target/starter-1.0.0-SNAPSHOT-fat.jar -cluster -cluster-public-port 17001 -cluster-public-host <YOUR-MACHINE-IP> -instance 4
```
**Important notes:**
The `-cluster` ensures verticles are running in cluster mode, so the event bus can communicate with all the verticles even if they are running in different machines.

`-cluster-public-port` is the public exposing port where the other clusters can connect to.

`-cluster-public-host` is **really important**. You should expose the IP of your machines so that other clusters can connect to it.

`-instance` is how many instances you want to run. Can be your core numbers, but better play with it. Especially each instance has a single eventloop.

## Test with Docker
Create jar:
```
./mvnw clean package
```
Or skip tests:
```
./mvnw clean package -Dmaven.test.skip=true
```
____________________
Docker build:
```
docker build -t vertx-async-gateway .
```

Docker run two instances exposed with different ports:
```
docker run -p 8888:8888 -e cluster-public-host=<YOUR-MACHINE-IP> -d vertx-async-gateway
docker run -p 8889:8888 -e cluster-public-host=<YOUR-MACHINE-IP> -d vertx-async-gateway
```

**To test properly use something like nginx to load balance among 8888 and 8889**
