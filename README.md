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
#### When a ticket is created:
   - The request is recorded with the provided title and description
   - The ticket is automatically marked as OPEN
   - A support agent is automatically assigned
   - The system returns the created ticket details
- Create Ticket API Endpoint:
```bash
POST /api/tickets
```

#### Required Headers
```bash
Authorization: Bearer <JWT_TOKEN>
```

#### Request Body Example
```json
{
  "title": "Unable to login",
  "description": "I am getting an error while logging into my account"
}
```
#### Successful Response Example
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
**2. Assign Ticket Feature**: This feature allows a support agent to assign an existing ticket to another support agent.

#### Endpoint

```
POST /api/tickets/{ticketId}/assign
```
#### Required Headers
```bash
Authorization: Bearer <JWT_TOKEN>
```
#### How to Use

1. Make sure the ticket already exists in the system.
2. Ensure both users exist and have role `SUPPORT_AGENT`.
3. Send a POST request with the IDs of:

    * User assigning the ticket
    * User receiving the ticket


#### Request Example

```json
{
  "assignedToUserId": "74379c78-6c36-49fc-bd9d-be64a92ddc3d",
  "assignedByUserId": "b71db4fd-38c7-4b01-b5f3-c28c81194b91"
}
```

#### Success Response , Created (201)

```json
{
  "success": true,
  "message": "Ticket assigned successfully",
  "data": {
    "id": "445ba19f-ac66-4048-aff9-d05363d648b5",
    "ticketId": "610a571b-380d-43f4-8b93-9816c9c9bb15",
    "assignedToUserId": "74379c78-6c36-49fc-bd9d-be64a92ddc3d",
    "assignedByUserId": "b71db4fd-38c7-4b01-b5f3-c28c81194b91"
  }
}
```

#### Business Rules

The assignment will fail if:

* Ticket does not exist
* Ticket status is `CLOSED`
* Either user does not exist
* Either user is not a `SUPPORT_AGENT`
* A user tries to assign the ticket to themselves

#### Example cURL

```bash
curl -X POST \
"http://localhost:8080/api/tickets/{ticketId}/assign" \
-H "Content-Type: application/json" \
-d '{
  "assignedToUserId": "<support-agent-id>",
  "assignedByUserId": "<assigning-agent-id>"
}'
```

**NOTE:** Replace `{ticketId}` and user IDs with valid UUIDs present in your database.

---

**3. View Ticket By ID**

#### For Support Agent Role:

- View Ticket (For Support Agent User) API Endpoint:
```bash
GET /api/tickets/{id}?role=agent
```
#### Required Headers
```bash
Authorization: Bearer <JWT_TOKEN>
```

- Successful Response Example
  HTTP Status: 200 SUCCESS
```json
{
    "success": true,
    "message": "Ticket fetched successfully",
    "data": {
        "title": "Unable to reset password",
        "description": "Customer reports that the password reset link expires immediately after clicking it.",
        "createdAt": "2026-02-17T09:15:00",
        "priority": "HIGH",
        "status": "OPEN"
    }
}
```

#### For Customer Role:

- View Ticket (For Customer User) API Endpoint:
```bash
GET /api/tickets/{id}?role=customer
```
#### Required Headers
```bash
Authorization: Bearer <JWT_TOKEN>
```

- Successful Response Example
  HTTP Status: 200 SUCCESS
```json
{
    "success": true,
    "message": "Ticket fetched successfully",
    "data": {
    "title": "Unable to reset password",
    "description": "Customer reports that the password reset link expires immediately after clicking it.",
    "status": "OPEN",
    "createdAt": "2026-02-17T09:15:00",
    "agentName": "Bob Johnson"
    }
}
```

---

**4. Add Comment to Ticket:** This feature allows authorized users to add comments to an existing ticket. Only the ticket creator or the assigned agent can add comments to that ticket.

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
Authorization: Bearer <JWT_TOKEN>
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

---

**5. View All Comments Of a Ticket:** Retrieve all comments associated with a specific ticket.
- Access is granted only if:
   - The user is the *ticket creator*, OR 
   - The user is the *assigned agent*

#### Endpoint
```bash
GET /api/tickets/{ticketId}/comments
```

#### Authentication Header

```bash
Authorization: Bearer <JWT_TOKEN>
```


#### Success Response (200)
```json
{
    "success": true,
    "message": "Comments retrieved successfully",
    "data": [
        {
        "body": "Issue resolved",
        "commenter": "Jatin",
        "createdAt": "2026-02-20T10:15:30"
        }
    ]
}
```

---

**6. Get All Tickets (Customer & Support Agent):** Allows authenticated users to retrieve all tickets based on their role. 

- Customers can view tickets they created
- Support Agents can view tickets assigned to them.

#### Endpoint
```bash
GET /api/tickets
```

#### Authentication Header

```bash
Authorization: Bearer <JWT_TOKEN>
```

#### Success Response for CUSTOMER
HTTP 200 — OK
```json
{
  "success": true,
  "message": "Tickets fetched successfully",
  "data": [
    {
      "id": "uuid",
      "title": "Login issue",
      "description": "Cannot login to the portal",
      "status": "OPEN",
      "createdAt": "2026-02-20T10:15:30",
      "agentName": "Rakshit"
    }
  ]
}
```

#### Success Response for SUPPORT_AGENT
HTTP 200 — OK
```json
{
  "success": true,
  "message": "Tickets fetched successfully",
  "data": [
    {
      "id": "uuid",
      "title": "Login issue",
      "description": "Cannot login to the portal",
      "status": "OPEN",
      "priority": "HIGH",
      "createdAt": "2026-02-20T10:15:30"
    }
  ]
}
```

---

**7. Update Ticket (Customer & Support Agent):**

Allows a **CUSTOMER** or **SUPPORT_AGENT** to update an existing ticket based on role-specific permissions.

**Endpoint:** 
```bash
PATCH /api/tickets/{ticketId}
```

#### Authentication Header

```bash
Authorization: Bearer <JWT_TOKEN>
```

**Role-Based Update Rules**

**CUSTOMER**

* Can update **description**
* Can update **status** (e.g., `CLOSED`)
* Cannot update **priority**
* Cannot update ticket once it is already `CLOSED`

**SUPPORT_AGENT**

* Can update **status**
* Can update **priority**
* Must be assigned to the ticket, else cannot update
* Cannot update ticket once it is already `CLOSED`
* Must provide at least one of:

  * `status`
  * `priority`
* Can only change ticket status:

  * From `OPEN` → `IN_PROGRESS` or `CLOSED`
  * From `IN_PROGRESS` → `CLOSED`

**Request Examples**

**Customer Request**

```json
{
  "description": "Updated description",
  "status": "CLOSED"
}
```

**Agent Request**

```json
{
  "priority": "LOW",
  "status": "CLOSED"
}
```

**Constraints & Validations**

**Common Validations**

* `ticketId` must be a valid UUID.
* Ticket must exist.
* Ticket must not already be `CLOSED`.
* Enum fields (`status`, `priority`) must contain valid values.

**Customer-Specific**

* Cannot update `priority`.
* Can only update their own ticket.

**Agent-Specific**

* Must be assigned to the ticket.
* At least one of `status` or `priority` must be provided.
* Status transitions allowed:

  * `OPEN` → `IN_PROGRESS` or `CLOSED`
  * `IN_PROGRESS` → `CLOSED`

**Success Response:** `200 OK`

**Customer Response**

```json
{
  "success": true,
  "message": "Ticket updated successfully",
  "data": {
    "title": "Improve ticket search performance",
    "description": "Updated description",
    "status": "CLOSED",
    "createdAt": "2026-02-15T14:22:31",
    "updatedAt": "2026-02-23T12:50:19.229691"
  }
}
```

**Agent Response**

```json
{
  "success": true,
  "message": "Ticket updated successfully",
  "data": {
    "title": "Improve ticket search performance",
    "description": "test123",
    "status": "CLOSED",
    "priority": "LOW",
    "createdAt": "2026-02-15T14:22:31",
    "updatedAt": "2026-02-23T12:50:19.229691"
  }
}
```

**Error Responses**

`400 Bad Request`

* Invalid enum value (`status` / `priority`)
* Agent request missing both `status` and `priority`
* Attempt to update restricted fields
* Ticket already `CLOSED`

Example:

```json
{
  "code": "BAD_REQUEST",
  "message": "At least one of status or priority must be provided"
}
```

`403 Forbidden`

* Agent attempting to update ticket not assigned to them
* Customer attempting to update another customer's ticket

`404 Not Found`

* Ticket does not exist

**Service Layer Test Cases**

* Customer successfully closing ticket
* Customer attempting to update priority
* Updating already `CLOSED` ticket
* Support agent updating assigned ticket
* Agent updating ticket not assigned to them
* Agent updating already `CLOSED` ticket

**Controller Layer Test Cases**

* Successful update → `200 OK`
* Invalid enum value in request → `400 Bad Request`
* Agent unauthorized update → `403 Forbidden`

---

**8. Get All Users By Role:** Allows authenticated `Support Agents` to retrieve all users filtered by role.

This is useful for scenarios like reassigning tickets, where an agent needs to fetch a list of available support agents.

**Endpoint:**
```bash
GET /api/users?role={ROLE}
```

#### Authentication Header
```bash
Authorization: Bearer <JWT_TOKEN>
```

**Query Params**
```bash
role=CUSTOMER | SUPPORT_AGENT
```

**Success Response for CUSTOMER**
HTTP 200 — OK
```json
{
  "success": true,
  "message": "Users fetched successfully",
  "data": [
    {
      "id": "uuid",
      "name": "Jatin",
      "email": "jatin@example.com",
      "role": "CUSTOMER"
    },
    {
      "id": "uuid",
      "name": "Raj",
      "email": "raj@example.com",
      "role": "CUSTOMER"
    }
  ]
}
```

---

**9. Register User (Customer only):** Allows new customers to create an account within the system without authentication configuration.

**Endpoint:** 
```bash
POST /api/auth/register
```

**Request Body:**

```json
{
  "name": "Jatin Joshi",
  "email": "jatin@gmail.com",
  "password": "Password@123"
}

```

**Constraints & Validations:**

* **Name:** Required, max 50 characters, must contain letters (cannot be purely numeric).
* **Email:** Required, must be a valid email format, max 100 characters.
* **Password:** Required, 8-16 characters. Must include at least:
* One uppercase letter
* One lowercase letter
* One digit
* One special character (`@#$%^&+=!`)

**Success Response:** `201 Created`

```json
{
  "success": true,
  "message": "User created successfully",
  "data": {
    "name": "Jatin Joshi",
    "email": "jatin@gmail.com",
    "id": "501a4912-4351-428b-a4ed-971e28ee1086"
  }
}

```

**Error Responses:**

* `400 Bad Request`: Validation failed (e.g., weak password or invalid email).
* `409 Conflict`: User with the provided email already exists.

##### Testing Modules
* **UserControllerTest:** Validates API contract, HTTP status codes, and JSON response structure.
* **UserServiceTest:** Validates business logic, password encoding, and duplicate email prevention.


**Error Responses:**

* `400 Bad Request`: Validation failed (e.g., weak password or invalid email).
* `409 Conflict`: User with the provided email already exists.

---
**10. Login User:**
Allows new customers to log in the system with proper authentication.

**Endpoint:**
```bash
POST /api/auth/login
```

**Request Body:**

```json
{
  "email": "Priyansh@gmail.com",
  "password": "Password@123"
}

```

**Constraints & Validations:**

* **Email:** Required
* **Password:** Required
**Success Response:** `200 OK`

```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "<JWT_TOKEN>",
    "userId": "UUID",
    "email": "saxena@gmail.com",
    "role": "CUSTOMER"
  }
}
```

**Error Responses:**

* `401 Unauthorized`: Invalid email or password.
```aiignore
{
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid email or password"
}
```

---

**11. Swagger OpenApi documentation**

This project uses **SpringDoc OpenAPI** to automatically generate interactive documentation for all REST endpoints. This allows you to visualize, explore, and test the API directly from your browser.



#### How to Access

Once the application is running, you can access the documentation at the following URLs:

* **Interactive UI (Swagger UI):** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
* **OpenAPI Specification (JSON):** [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

#### Testing Endpoints

1.  Open the **Swagger UI** link above.
2.  Click on any endpoint (e.g., `POST /api/auth/register`).
3.  Click the **"Try it out"** button.
4.  The request body will be pre-filled with example data defined in our `@Schema` annotations.
5.  Edit the data if needed and click **"Execute"** to see the real response from the server.

---

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