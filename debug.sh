set -e

echo "Starting Eureka Server in debug mode..."
cd eureka-server
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" &
cd ..

echo "Starting Auth Service in debug mode..."
cd auth-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5006" &
cd ..

echo "Starting User Service in debug mode..."
cd user-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5007" &
cd ..

echo "Starting Train Service in debug mode..."
cd train-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5008" &
cd ..

echo "Starting Booking Service in debug mode..."
cd booking-service
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5009" &
cd ..

echo "Starting Frontend..."
cd frontend
npm run dev &
cd ..

echo "All services started in debug mode!"
wait