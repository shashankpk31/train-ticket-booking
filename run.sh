#!/bin/bash
set -e

echo "Starting Eureka Server..."
cd eureka-server
mvn spring-boot:run &
cd ..

echo "Starting Auth Service..."
cd auth-service
mvn spring-boot:run &
cd ..

echo "Starting User Service..."
cd user-service
mvn spring-boot:run &
cd ..

echo "Starting Train Service..."
cd train-service
mvn spring-boot:run &
cd ..

echo "Starting Booking Service..."
cd booking-service
mvn spring-boot:run &
cd ..

echo "Starting Frontend..."
cd frontend
npm run dev &
cd ..

echo "All services started!"
wait