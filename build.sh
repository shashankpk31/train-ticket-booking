#!/bin/bash
set -e

echo "Building Eureka Server..."
cd eureka-server
mvn clean install
cd ..

echo "Building Auth Service..."
cd auth-service
mvn clean install
cd ..

echo "Building User Service..."
cd user-service
mvn clean install
cd ..

echo "Building Train Service..."
cd train-service
mvn clean install
cd ..

echo "Building Booking Service..."
cd booking-service
mvn clean install
cd ..

echo "Building Frontend..."
cd frontend
npm install
npm run build
cd ..


echo "Build completed!"