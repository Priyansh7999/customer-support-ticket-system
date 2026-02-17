## Customer Support Ticket System
Build a Customer Support Ticket System that allows users to raise support tickets and enables support agents to
manage them effectively. The system should support ticket creation, assignment, status updates, commenting,
and closing of tickets.

## How to Run the Project
### Prerequisites
Make sure you have these installed before starting:
- Java
- PostgreSQL
- pgAdmin

### 1. Clone the Repository

```bash
git clone https://github.com/Priyansh7999/customer-support-ticket-system.git
cd customer-support-ticket-system
```

### 2. Database Setup

#### Step 1: Install PostgreSQL
Download and install PostgreSQL from (https://www.postgresql.org/download/)

#### Step 2: Create Database using pgAdmin
1. Open pgAdmin
2. Connect to your PostgreSQL server
2. Create database name: customer_support_ticket_system

#### Step 3: Configure application.properties
- Main
```env
spring.application.name=customer-support-ticket-system
spring.datasource.url=jdbc:postgresql://localhost:5432/customer_support_ticket_system
spring.datasource.username=postgres
spring.datasource.password=<YOUR_DB_PASSWORD>
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.defer-datasource-initialization=true
management.endpoints.web.exposure.include=health
management.endpoint.health.show-details=always
```
- Test
```
spring.application.name=customer-support-ticket-system
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
```
### 3. Start the Server
```bash
./gradlew run
```

### 4. The server will start on:
```
http://localhost:8080
```
