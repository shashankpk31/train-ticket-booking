# Train Ticket Booking System Context

## Project Overview

This is a **Spring Boot-based microservices system** for a train ticket booking application, built with **Spring Boot 3.2.5**, **Java 17**, and **Maven**. It consists of **six microservices**:

- **Eureka Server**: Service discovery.
- **API Gateway**: Routes requests, handles authentication/authorization.
- **Train Service**: Manages train schedules and details.
- **Inventory Service**: Tracks seat availability.
- **Booking Service**: Processes bookings and emits events to Kafka.
- **Notification Service**: Consumes booking events via Kafka and sends notifications.

The system uses **Eureka** for service discovery, **Spring Cloud Gateway** for routing, **PostgreSQL** for data storage, **Kafka** for messaging, and **Docker** for containerization. Authentication is centralized in the API Gateway using **JWT** and **Spring WebFlux Security**, with user data stored in a PostgreSQL database.

## Architecture

- **Microservices**:
  - **Eureka Server** (port 8761): Registers and discovers services.
  - **API Gateway** (port 8080): Routes requests to microservices, authenticates via JWT, and adds `X-Auth-User` and `X-Auth-Roles` headers.
  - **Train Service** (port 8081): Handles train data (e.g., `/trains?source=NYC&destination=CHI`).
  - **Inventory Service** (port 8082): Manages seat inventory.
  - **Booking Service** (port 8083): Creates bookings, stores in PostgreSQL, and publishes to Kafka.
  - **Notification Service** (port 8084): Consumes Kafka events and sends notifications.
- **Interactions**:
  - API Gateway routes requests to services using `lb://<service-name>` (Eureka load balancing).
  - Booking Service sends booking events to Kafka; Notification Service consumes them.
  - All services register with Eureka Server.
- **Security**:
  - Centralized in API Gateway with JWT-based authentication.
  - Public endpoints: `/auth/register`, `/auth/login`, `/actuator/**`.
  - Protected endpoints: `/trains/**`, `/inventory/**`, `/bookings/**` require `ROLE_USER` or `ROLE_ADMIN`.
  - Users stored in `apigatewaydb.users` table (username, BCrypt-hashed password, roles).

## Dependencies (pom.xml)

- **Common Dependencies** (all microservices):
  - `spring-boot-starter-actuator`
  - `spring-boot-starter-test` (scope: test)
  - `spring-cloud-starter-netflix-eureka-client`
  - `lombok`
- **Eureka Server**:
  - `spring-cloud-starter-netflix-eureka-server`
- **API Gateway**:
  - `spring-boot-starter-webflux`
  - `spring-boot-starter-security`
  - `spring-boot-starter-data-jpa`
  - `postgresql` (runtime)
  - `jjwt-api`, `jjwt-impl`, `jjwt-jackson` (0.11.5)
  - `spring-cloud-starter-gateway`
- **Train Service, Inventory Service, Booking Service**:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `postgresql` (runtime)
- **Booking Service (additional)**:
  - `spring-boot-starter-webflux` (for reactive Kafka)
  - `spring-kafka`
- **Notification Service**:
  - `spring-boot-starter-web`
  - `spring-kafka`
- **Dependency Management**:
  - `spring-cloud-dependencies` (2023.0.1)

## Configurations

- **Eureka Server** (`application.yml`):

  ```yaml
  spring:
    application:
      name: eureka-server
  server:
    port: 8761
  eureka:
    client:
      register-with-eureka: false
      fetch-registry: false
  ```

- **API Gateway** (`application.yml`):

  ```yaml
  spring:
    application:
      name: api-gateway
    datasource:
      url: jdbc:postgresql://postgres:5432/apigatewaydb
      username: postgres
      password: postgres
      driver-class-name: org.postgresql.Driver
    jpa:
      hibernate:
        ddl-auto: update
      properties:
        hibernate:
          dialect: org.hibernate.dialect.PostgreSQLDialect
    cloud:
      gateway:
        discovery:
          locator:
            enabled: true
            lower-case-service-id: true
        routes:
          - id: train-service
            uri: lb://train-service
            predicates:
              - Path=/trains/**
          - id: inventory-service
            uri: lb://inventory-service
            predicates:
              - Path=/inventory/**
          - id: booking-service
            uri: lb://booking-service
            predicates:
              - Path=/bookings/**
  server:
    port: 8080
  eureka:
    client:
      service-url:
        defaultZone: http://eureka-server:8761/eureka
  security:
    jwt:
      secret-key: <32-byte-secret-key>
      expiration: 86400000
  ```

- **Train Service** (`application.properties`):

  ```properties
  spring.application.name=train-service
  server.port=8081
  spring.datasource.url=jdbc:postgresql://postgres:5432/traindb
  spring.datasource.username=postgres
  spring.datasource.password=postgres
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
  eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
  ```

- **Inventory Service** (`application.properties`): Similar to Train Service, port 8082, database `inventorydb`.

- **Booking Service** (`application.properties`):

  ```properties
  spring.application.name=booking-service
  server.port=8083
  spring.datasource.url=jdbc:postgresql://postgres:5432/bookingdb
  spring.datasource.username=postgres
  spring.datasource.password=postgres
  spring.jpa.hibernate.ddl-auto=update
  spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
  spring.kafka.bootstrap-servers=kafka:9092
  eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
  ```

- **Notification Service** (`application.properties`):

  ```properties
  spring.application.name=notification-service
  server.port=8084
  spring.kafka.bootstrap-servers=kafka:9092
  eureka.client.service-url.defaultZone=http://eureka-server:8761/eureka
  ```

- **Docker Compose** (`docker-compose.yml`):

  ```yaml
  version: '3.8'
  services:
    eureka-server:
      image: eureka-server
      build: ./eureka-server
      ports:
        - "8761:8761"
    api-gateway:
      image: api-gateway
      build: ./api-gateway
      ports:
        - "8080:8080"
      depends_on:
        - eureka-server
        - postgres
      environment:
        - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/apigatewaydb
        - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
    train-service:
      image: train-service
      build: ./train-service
      ports:
        - "8081:8081"
      depends_on:
        - postgres
        - eureka-server
      environment:
        - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/traindb
        - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
    inventory-service:
      image: inventory-service
      build: ./inventory-service
      ports:
        - "8082:8082"
      depends_on:
        - postgres
        - eureka-server
      environment:
        - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/inventorydb
        - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
    booking-service:
      image: booking-service
      build: ./booking-service
      ports:
        - "8083:8083"
      depends_on:
        - postgres
        - kafka
        - eureka-server
      environment:
        - SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/bookingdb
        - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
        - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
    notification-service:
      image: notification-service
      build: ./notification-service
      ports:
        - "8084:8084"
      depends_on:
        - kafka
        - eureka-server
      environment:
        - SPRING_KAFKA_BOOTSTRAP_SERVERS=kafka:9092
        - EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-server:8761/eureka
    postgres:
      image: postgres:15
      environment:
        - POSTGRES_USER=postgres
        - POSTGRES_PASSWORD=postgres
        - POSTGRES_MULTIPLE_DATABASES=apigatewaydb,traindb,inventorydb,bookingdb
      ports:
        - "5432:5432"
      volumes:
        - postgres-data:/var/lib/postgresql/data
        - ./init-databases.sh:/docker-entrypoint-initdb.d/init-databases.sh
    zookeeper:
      image: confluentinc/cp-zookeeper:latest
      environment:
        ZOOKEEPER_CLIENT_PORT: 2181
        ZOOKEEPER_TICK_TIME: 2000
      ports:
        - "2181:2181"
    kafka:
      image: confluentinc/cp-kafka:latest
      depends_on:
        - zookeeper
      environment:
        KAFKA_BROKER_ID: 1
        KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
        KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:9092
        KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      ports:
        - "9092:9092"
  volumes:
    postgres-data:
  ```

- **init-databases.sh**:

  ```bash
  #!/bin/bash
  psql -U postgres -c "CREATE DATABASE apigatewaydb;"
  psql -U postgres -c "CREATE DATABASE traindb;"
  psql -U postgres -c "CREATE DATABASE inventorydb;"
  psql -U postgres -c "CREATE DATABASE bookingdb;"
  ```

## Security Setup

- **Location**: Centralized in API Gateway.

- **Authentication**:

  - **JWT**: Tokens generated with `io.jsonwebtoken:jjwt` (0.11.5), signed with `security.jwt.secret-key` (32-byte key), valid for 24 hours (`security.jwt.expiration=86400000`).

  - **Endpoints**:

    - `/auth/register`: Creates user in `apigatewaydb.users` (username, BCrypt-hashed password, roles).
    - `/auth/login`: Validates credentials, returns JWT.

  - **Users Table** (PostgreSQL):

    ```sql
    CREATE TABLE users (
        username VARCHAR(255) PRIMARY KEY,
        password VARCHAR(255) NOT NULL,
        roles VARCHAR(255) NOT NULL
    );
    ```

- **Authorization**:

  - Protected endpoints (`/trains/**`, `/inventory/**`, `/bookings/**`) require `ROLE_USER` or `ROLE_ADMIN`.
  - `JwtAuthenticationFilter` validates JWT, adds `X-Auth-User` and `X-Auth-Roles` headers.

- **Key Classes**:

  - **JwtUtil**: Generates/validates JWTs using `@Value` for `secretKey` and `expiration`.
  - **CustomUserDetails**: Implements `UserDetails` for username, password, roles.
  - **CustomReactiveUserDetailsService**: Loads/saves users from `apigatewaydb` via JPA.
  - **SecurityConfig**: Configures `@EnableWebFluxSecurity`, `SecurityWebFilterChain`, and `JwtAuthenticationFilter`.
  - **AuthenticationConfig**: Defines `ReactiveAuthenticationManager` for JWT validation.
  - **AuthController**: Handles `/auth/register` and `/auth/login`.

## Rebuilding Logic

To recreate the system:

1. **Setup Project Structure**:
   - Create Maven projects for each microservice: `eureka-server`, `api-gateway`, `train-service`, `inventory-service`, `booking-service`, `notification-service`.
   - Directory: `Three Projects/<service-name>`.
2. **Dependencies**:
   - Add `pom.xml` dependencies as listed above.
   - Use `spring-cloud-dependencies:2023.0.1` for dependency management.
3. **Configurations**:
   - Copy `application.yml` or `application.properties` for each service.
   - Set `security.jwt.secret-key` (generate with `openssl rand -base64 32`).
4. **Database**:
   - Run PostgreSQL with `docker-compose.yml`.
   - Execute `init-databases.sh` to create databases.
   - Create `users` table in `apigatewaydb`.
5. **Security**:
   - **API Gateway**:
     - Implement `JwtUtil` for JWT generation/validation.
     - Create `CustomUserDetails` with `UserDetails` interface.
     - Implement `CustomReactiveUserDetailsService` with JPA for user management.
     - Configure `SecurityConfig` with `@EnableWebFluxSecurity`, `SecurityWebFilterChain`, and `JwtAuthenticationFilter`.
     - Add `AuthenticationConfig` for `ReactiveAuthenticationManager`.
     - Implement `AuthController` for `/auth/register` and `/auth/login`.
   - **Downstream Services**:
     - Use `X-Auth-User` and `X-Auth-Roles` headers for authorization if needed.
6. **Microservices Logic**:
   - **Eureka Server**: Enable with `@EnableEurekaServer`.
   - **Train Service**: REST endpoints for train schedules (e.g., `GET /trains?source&destination`).
   - **Inventory Service**: REST endpoints for seat availability.
   - **Booking Service**: REST endpoints for bookings, publish events to Kafka topic (e.g., `booking-events`).
   - **Notification Service**: Consume Kafka events, send notifications (e.g., email or log).
7. **Kafka**:
   - Configure Kafka producer in Booking Service (`spring-kafka`).
   - Configure Kafka consumer in Notification Service.
8. **Docker**:
   - Use provided `docker-compose.yml` to run services, PostgreSQL, Zookeeper, and Kafka.
   - Build each service with `mvn clean install` and `docker-compose up --build`.
9. **Testing**:
   - Register user: `curl -X POST http://localhost:8080/auth/register -d '{"username":"user1","password":"password","roles":"USER"}'`
   - Login: `curl -X POST http://localhost:8080/auth/login -d '{"username":"user1","password":"password"}'`
   - Protected endpoint: `curl http://localhost:8080/trains?source=NYC&destination=CHI -H "Authorization: Bearer <JWT>"`

## Key Notes

- **Java Version**: 17.
- **Spring Boot**: 3.2.5.
- **Database**: PostgreSQL 15, databases: `apigatewaydb`, `traindb`, `inventorydb`, `bookingdb`.
- **Kafka**: Confluent Kafka, broker at `kafka:9092`, Zookeeper at `zookeeper:2181`.
- **Security**: BCrypt for password hashing, JWT for authentication.
- **Eureka**: Service discovery at `http://localhost:8761`.

This context provides the blueprint to rebuild the system, focusing on architecture, configurations, and core logic without duplicating code files.