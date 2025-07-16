 #!/bin/bash
 set -e

 echo "Creating databases: apigatewaydb, traindb, inventorydb, bookingdb..."
 psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname postgres <<-EOSQL
   CREATE DATABASE apigatewaydb;
   CREATE DATABASE traindb;
   CREATE DATABASE inventorydb;
   CREATE DATABASE bookingdb;
   GRANT ALL PRIVILEGES ON DATABASE apigatewaydb TO $POSTGRES_USER;
   GRANT ALL PRIVILEGES ON DATABASE traindb TO $POSTGRES_USER;
   GRANT ALL PRIVILEGES ON DATABASE inventorydb TO $POSTGRES_USER;
   GRANT ALL PRIVILEGES ON DATABASE bookingdb TO $POSTGRES_USER;
   \c apigatewaydb
   CREATE TABLE users (
     username VARCHAR(255) PRIMARY KEY,
     password VARCHAR(255) NOT NULL,
     roles VARCHAR(255) NOT NULL,
     first_name VARCHAR(255),
     last_name VARCHAR(255),
     email VARCHAR(255),
     phone_number VARCHAR(20),
     address TEXT,
     date_of_birth VARCHAR(10),
     id_proof VARCHAR(50)
   );
 EOSQL
 echo "Databases and users table created successfully."
