cd ./api-gateway
mvn clean package
cd ..
cd ./train-service
mvn clean package
cd ..
cd ./inventory-service
mvn clean package
cd .. 
cd ./notification-service 
mvn clean package
cd ..
cd ./booking-service
mvn clean package
cd ..
cd eureka-server/
mvn clean package
cd ..
docker compose build api-gateway train-service inventory-service notification-service booking-service
docker compose up -d
docker image prune