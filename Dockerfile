FROM openjdk:11-jre-slim
# copy application WAR (with libraries inside)
COPY target/starter-1.0.0-SNAPSHOT-fat.jar /app.jar
# specify default command
CMD ["java", "-jar", "/app.jar", "-cluster"]
