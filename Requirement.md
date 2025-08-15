RailwayTicketBooking Project Requirements
Project Overview

Project Name: RailwayTicketBooking
Description: A railway ticket booking system similar to IRCTC, focusing on transactional integrity, data consistency, and user experience. Users can register/login via Keycloak, search trains, check seat availability, book tickets, and view booking history. Managers can manage trains, routes, schedules, and bookings. The system is designed with a minimal microservices architecture for maintainability, with potential extensions for notifications, payments, and search.
Target Users: General users (passengers) and managers (admin).
Key Goals: Facilitate efficient train ticket booking with robust transaction management and scalable architecture.

Architecture

Backend Framework: Spring Boot, Spring Data JPA, PostgreSQL, Spring Cloud Eureka (service discovery), Spring Cloud Gateway (API gateway), Kafka (async messaging), Lombok, MapStruct, Resilience4j.
Frontend Framework: React, Tailwind CSS, React Router DOM, Axios, React Query, Zustand, Keycloak JS Adapter, React Toastify.
Authentication/Authorization: Keycloak with OAuth2 and JWT for role-based access (user, manager).
Microservices:
Auth Service: Handles Keycloak integration for authentication and authorization.
User Service: Manages user profiles and booking history.
Train Service: Manages trains, routes, stations, schedules, and seat availability.
Booking Service: Handles ticket bookings, seat locking, payment processing, and notifications.


Potential Future Microservices:
Notification Service: Currently handled within the Booking Service using Kafka for async delivery (e.g., email/SMS for booking confirmations). Extract into a separate service if notification complexity (e.g., SMS integration, queued delivery) increases.
Payment Service: Currently managed within the Booking Service with Kafka for async payment processing. Extract into a separate service if payment complexity grows (e.g., multiple gateways, refund processing).
Search Service: Currently handled within the Train Service with Redis caching for performance. Extract into a separate service if search performance becomes a bottleneck (e.g., high traffic requiring optimized indexing like Elasticsearch).



Database and Storage

Primary Database: PostgreSQL (separate schema per microservice).
Caching: Redis for static data (trains, stations, routes), search results, and approximate seat availability. Ehcache for JPA second-level caching.
File Storage: AWS S3 for static assets (frontend build).

Database Design and Data Dictionary
Auth Service

Schema: auth_db
Tables:
auth_users:
id (UUID, PK): Matches Keycloak user ID.
email (VARCHAR, UNIQUE): User's email.
created_at (TIMESTAMP): Account creation time.
updated_at (TIMESTAMP): Last update time.





User Service

Schema: user_db
Tables:
users:
id (UUID, PK): Matches Keycloak user ID.
first_name (VARCHAR): First name.
last_name (VARCHAR): Last name.
phone_number (VARCHAR): Phone number.
created_at (TIMESTAMP): Creation time.
updated_at (TIMESTAMP): Last update time.


booking_history:
id (UUID, PK): History record ID.
user_id (UUID, FK): References users.
booking_id (UUID, FK): References bookings (Booking Service).
pnr_number (VARCHAR): PNR number.
journey_date (DATE): Travel date.
status (VARCHAR): CONFIRMED, CANCELLED, WAITLISTED.





Train Service

Schema: train_db
Tables:
stations:
id (UUID, PK): Station ID.
station_code (VARCHAR, UNIQUE): Station code (e.g., NDLS).
station_name (VARCHAR): Station name.
location (VARCHAR): Geographic coordinates.


trains:
id (UUID, PK): Train ID.
train_number (VARCHAR, UNIQUE): Train number.
train_name (VARCHAR): Train name.


routes:
id (UUID, PK): Route ID.
train_id (UUID, FK): References trains.
source_station_id (UUID, FK): References stations.
destination_station_id (UUID, FK): References stations.
total_distance (INTEGER): Route distance.


route_stations:
id (UUID, PK): Junction ID.
route_id (UUID, FK): References routes.
station_id (UUID, FK): References stations.
sequence_number (INTEGER): Station order.
arrival_time (TIME): Arrival time.
departure_time (TIME): Departure time.


schedules:
id (UUID, PK): Schedule ID.
train_id (UUID, FK): References trains.
days_of_operation (VARCHAR): Operating days.


coaches:
id (UUID, PK): Coach ID.
train_id (UUID, FK): References trains.
coach_type (VARCHAR): AC, Sleeper, etc.
total_seats (INTEGER): Total seats.


seat_availability:
id (UUID, PK): Availability ID.
train_id (UUID, FK): References trains.
journey_date (DATE): Travel date.
coach_type (VARCHAR): Coach type.
available_seats (INTEGER): Available seats.
version (INTEGER): For optimistic locking.





Booking Service

Schema: booking_db
Tables:
bookings:
id (UUID, PK): Booking ID.
user_id (UUID, FK): References users (User Service).
train_id (UUID, FK): References trains (Train Service).
journey_date (DATE): Travel date.
source_station_id (UUID, FK): References stations.
destination_station_id (UUID, FK): References stations.
pnr_number (VARCHAR, UNIQUE): PNR number.
status (VARCHAR): CONFIRMED, PENDING, CANCELLED, WAITLISTED.
total_amount (DECIMAL): Booking amount.
created_at (TIMESTAMP): Creation time.


passengers:
id (UUID, PK): Passenger ID.
booking_id (UUID, FK): References bookings.
name (VARCHAR): Passenger name.
age (INTEGER): Age.
gender (VARCHAR): M, F, O.
seat_number (VARCHAR): Seat number.


seat_locks:
id (UUID, PK): Lock ID.
train_id (UUID, FK): References trains.
journey_date (DATE): Travel date.
coach_type (VARCHAR): Coach type.
seat_number (VARCHAR): Locked seat.
user_id (UUID, FK): References users.
expiry_time (TIMESTAMP): Lock expiration.


payments:
id (UUID, PK): Payment ID.
booking_id (UUID, FK): References bookings.
transaction_id (VARCHAR): Payment gateway ID.
amount (DECIMAL): Paid amount.
status (VARCHAR): SUCCESS, FAILED, PENDING.
paid_at (TIMESTAMP): Payment time.





Transaction Management

Pessimistic Locking: Use SELECT ... FOR UPDATE on seat_availability during booking.
Optimistic Locking: Use version column in seat_availability for conflict detection.
Saga Pattern: Use Kafka for distributed transactions (e.g., booking and payment processing, notifications).

Features and Functionality
User Features

Register/Login: Via Keycloak.
Search Trains: Search by source, destination, and date (handled by Train Service with Redis caching).
Book Tickets: Select train, coach, seats, add passengers, and pay (handled by Booking Service with Kafka for async payments).
View Booking History: List past/upcoming bookings (User Service).
Cancel Tickets: Cancel bookings and release seats (Booking Service).

Manager Features

Manage Trains: Add/update trains, routes, schedules, coaches (Train Service).
Monitor Bookings: View booking stats and revenue (Booking Service).
Manage Seat Inventory: Adjust seat availability (Train Service).

Frontend Setup

Framework: React SPA.
Libraries:
React Router DOM (routing).
Axios (API requests).
Keycloak JS Adapter (authentication).
React Query (caching/async state).
Zustand (state management).
Tailwind CSS (styling).
React Toastify (notifications).


Folder Structure:src/
  ├── components/        # Reusable UI components
  ├── features/         # Feature modules (auth, trains, bookings, profile)
  ├── hooks/            # Custom hooks
  ├── context/          # Keycloak context
  ├── services/         # API services
  └── styles/           # Tailwind CSS config



Caching Strategy

Redis:
Cache static data (stations, trains, routes, schedules) with long TTL (24h).
Cache search results (Train Service) with short TTL (5m).
Cache approximate seat availability with very short TTL (1m).
Use Cache-Aside strategy.


Ehcache: JPA second-level cache for stations, trains in Train Service.

Notification Strategy

Current Approach: Notifications (e.g., booking confirmations, cancellations) are handled within the Booking Service using Kafka for async delivery (e.g., via Spring’s JavaMailSender for email).
Future Extension: Extract into a separate Notification Service if complexity increases (e.g., SMS integration, queued delivery, or multi-channel notifications).

Payment Strategy

Current Approach: Payment processing (e.g., sandbox gateway integration) is managed within the Booking Service, with Kafka for async transaction handling.
Future Extension: Extract into a separate Payment Service if complexity grows (e.g., multiple payment gateways, refund processing, or advanced retry mechanisms).

Search Strategy

Current Approach: Train search (source, destination, date) is handled by the Train Service, with Redis caching to optimize performance.
Future Extension: Extract into a separate Search Service if search performance becomes a bottleneck (e.g., high traffic requiring optimized indexing like Elasticsearch).

Deployment and Environment

Development Tools:
IDE: Spring Tool Suite (backend), VS Code (frontend).
Build Tool: Maven.
Async Messaging: Kafka (for payments, notifications).
Monitoring: Prometheus + Grafana (optional).


Containerization: Docker.
CI/CD: GitHub Actions.
Hosting: AWS Free Tier (EC2, RDS, S3).
Environment Variables:
DB_URL: PostgreSQL connection string.
DB_USER, DB_PASS: Database credentials.
KEYCLOAK_URL, KEYCLOAK_REALM, KEYCLOAK_CLIENT_ID, KEYCLOAK_CLIENT_SECRET: Keycloak config.
EUREKA_SERVER_URL: Eureka URL.
REDIS_HOST, REDIS_PORT: Redis config.
KAFKA_BROKER_URL: Kafka broker.
API_GATEWAY_URL: API gateway URL.



Development Strategy (MVA)

Phase 1: Foundation (1-2 months):
Auth Service with Keycloak.
User Service for profiles.
Train Service for trains, routes, schedules, and search.
Frontend with login and train search.
Deploy to AWS with Docker.


Phase 2: Booking and Payment (1-2 months):
Booking Service for seat availability, booking, payment, and notifications.
Transactional logic with locking.
Kafka for async payments and notifications.
Frontend booking flow.


Phase 3: Enhancements (1 month):
Booking history and cancellation.
Redis caching for search and static data.
Manager features for train/schedule management.
UI/UX refinement with Tailwind CSS.


Phase 4: Optimization (1 month):
Optimize with Redis/Ehcache.
Monitoring setup with Prometheus/Grafana.
Load testing on AWS.



Additional Notes

Lombok: Use for boilerplate reduction.
MapStruct: For entity-DTO mapping.
Resilience4j: For fault tolerance in inter-service calls.
Spring Cloud Gateway: For API routing and authentication.
Extensibility: The architecture supports adding Notification, Payment, or Search Services later if needed, with minimal changes due to Spring Cloud Gateway and Eureka.
