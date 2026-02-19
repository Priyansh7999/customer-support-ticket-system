## Customer Support Ticket System
Build a Customer Support Ticket System that allows users to raise support tickets and enables support agents to
manage them effectively. The system should support ticket creation, assignment, status updates, commenting,
and closing of tickets.

## Technologies Used
- Java (17+)
- Spring Boot (3.0+)
- Spring Data JPA
- PostgreSQL (for production)
- H2 Database (for testing)
- Gradle
- Postman (for API testing)

---

## Features
**1. Ticket Creation**: Customers can create support tickets with details such as title, description.
- When a ticket is created:
   - The request is recorded with the provided title and description
   - The ticket is automatically marked as OPEN
   - A support agent is automatically assigned
   - The system returns the created ticket details
- Create Ticket API Endpoint:
```bash
POST /api/tickets
```

- Required Headers
```bash
User-Id: <UUID of CUSTOMER>
```
- Example:
```bash
User-Id: 5e312015-35c7-48f7-80fa-67d084e70762
```
- Request Body Example
```json
{
  "title": "Unable to login",
  "description": "I am getting an error while logging into my account"
}
```
- Successful Response Example
HTTP Status: 201 CREATED
```json
{
  "success": true,
  "message": "Ticket created successfully",
  "data": {
    "id": "0bb729e9-a932-4564-8f18-96aad738f453",
    "title": "Unable to login",
    "description": "I am getting an error while logging into my account",
    "status": "OPEN",
    "assignedToName": "Support Agent Name",
    "createdAt": "2026-02-18T10:15:30"
  }
}
```
---

### 4. Add Comment to Ticket

This feature allows authorized users to add comments to an existing ticket. Only the ticket creator or the assigned agent can add comments to that ticket.

#### Logic Flow

- Validate request body.
- Fetch User by ID.
- Fetch Ticket by ID.
- Check if user belongs to the ticket.
- Create and save Comment.
- Return response with comment `id`, `body`, and `createdAt`.

#### API Endpoint
```
POST /api/tickets/{ticketId}/comments
```

#### Headers
```
User-Id: <userId>
```

#### Request Body
```json
{
  "body": "i have a issue"
}
```

#### Response Body
```json
{
  "success": true,
  "message": "Comment added successfully",
  "data": {
    "id": "ea6aa95a-bee0-46c4-b63f-4641f5750dce",
    "body": "i have a issue",
    "createdAt": "2026-02-18T12:21:12.895856"
  }
}
```
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
3. Create database name: customer_support_ticket_system

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


## Feature Implemented




### Assign Ticket Feature

This feature allows a support agent to assign an existing ticket to another support agent.

---

#### Endpoint

```
POST /api/tickets/{ticketId}/assign
```

---

### How to Use

1. Make sure the ticket already exists in the system.
2. Ensure both users exist and have role `SUPPORT_AGENT`.
3. Send a POST request with the IDs of:

    * User assigning the ticket
    * User receiving the ticket

---

### Request Example

```json
{
  "assignedToUserId": "74379c78-6c36-49fc-bd9d-be64a92ddc3d",
  "assignedByUserId": "b71db4fd-38c7-4b01-b5f3-c28c81194b91"
}
```

---

### Success Response

```json
{
  "success": true,
  "message": "Ticket assigned successfully",
  "data": {
    "id": "445ba19f-ac66-4048-aff9-d05363d648b5",
    "ticketId": "610a571b-380d-43f4-8b93-9816c9c9bb15",
    "assignedToUserId": "74379c78-6c36-49fc-bd9d-be64a92ddc3d",
    "assignedByUserId": "b71db4fd-38c7-4b01-b5f3-c28c81194b91",
    "message": "Ticket Assigned Successfully"
  }
}
```

---

### Business Rules

The assignment will fail if:

* Ticket does not exist
* Ticket status is `CLOSED`
* Either user does not exist
* Either user is not a `SUPPORT_AGENT`
* A user tries to assign the ticket to themselves

---

### Example cURL

```bash
curl -X POST \
"http://localhost:8080/api/tickets/{ticketId}/assign" \
-H "Content-Type: application/json" \
-d '{
  "assignedToUserId": "<support-agent-id>",
  "assignedByUserId": "<assigning-agent-id>"
}'
```

---

**NOTE:** Replace `{ticketId}` and user IDs with valid UUIDs present in your database.
