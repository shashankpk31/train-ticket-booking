#!/bin/bash
psql -U postgres -c "CREATE DATABASE apigatewaydb;"
psql -U postgres -c "CREATE DATABASE traindb;"
psql -U postgres -c "CREATE DATABASE inventorydb;"
psql -U postgres -c "CREATE DATABASE bookingdb;"