FROM openjdk:11-jdk-slim
WORKDIR /app
COPY target/backend-1.0-SNAPSHOT.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]