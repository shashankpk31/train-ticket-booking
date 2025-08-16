FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/eureka-server-1.0.0-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]