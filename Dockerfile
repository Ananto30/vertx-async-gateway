FROM openjdk:11-jre-slim

COPY target/starter-1.0.0-SNAPSHOT-fat.jar /app.jar

ENV CLUSTER_PUBLIC_PORT 17001
ENV CLUSTER_PUBLIC_HOST 192.168.0.100

CMD java -jar /app.jar -cluster -cluster-public-port $CLUSTER_PUBLIC_PORT -cluster-public-host $CLUSTER_PUBLIC_HOST -instance 4
