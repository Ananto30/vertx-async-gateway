# Vertx Async to Sync

Example of converting (legacy) async service to sync.

## IMPORTANT!

Change the `EVENT_BUS_CLUSTER_PUBLIC_HOST` with your machine IP. (for dev)


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
docker run -p 8888:8888 -d vertx-async-gateway
docker run -p 8889:8888 -d vertx-async-gateway
```

**To test properly use something like nginx to load balance among 8888 and 8889**
