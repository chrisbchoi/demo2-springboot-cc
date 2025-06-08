# Spring Boot User Management Demo

A Spring Boot application that demonstrates user management with RESTful APIs, Spring Security, and JPA with H2 database.

## Table of Contents

- [Project Overview](#project-overview)
- [Technologies](#technologies)
- [Project Structure](#project-structure)
- [Features](#features)
- [Getting Started](#getting-started)
  - [Prerequisites](#prerequisites)
  - [Installation](#installation)
  - [Running the Application](#running-the-application)
- [API Documentation](#api-documentation)
- [Testing](#testing)
- [Security](#security)
- [Database Configuration](#database-configuration)

## Project Overview

This Spring Boot application demonstrates best practices for building a secure RESTful API for user management. It implements CRUD operations for a User entity, with proper error handling, unit testing, and security configuration.

## Technologies

- **Java** 24
- **Spring Boot** 3.5.0
- **Spring Security** - For authentication and authorization
- **Spring Data JPA** - For database operations
- **H2 Database** - In-memory database for development
- **JUnit 5** - For unit testing
- **Mockito** - For mocking in tests
- **Maven** - For dependency management and build

## Project Structure

The project follows a standard layered architecture:

- **Model** - Entity classes (`User`)
- **Repository** - Data access interfaces
- **Service** - Business logic
- **Controller** - REST API endpoints
- **Exception** - Custom exceptions and error handling
- **Config** - Application configuration classes (Security, etc.)
- **Test** - Unit and integration tests

```
src/
├── main/
│   ├── java/com/cc/data/demo2springboot/
│   │   ├── Demo2SpringbootApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   └── UserController.java
│   │   ├── exception/
│   │   │   └── ResourceNotFoundException.java
│   │   ├── model/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   └── service/
│   │       └── UserService.java
│   └── resources/
│       ├── application.properties
│       ├── openapi.yaml
│       └── user-requests.http
└── test/
    └── java/com/cc/data/demo2springboot/
        ├── controller/
        │   └── UserControllerTest.java
        ├── service/
        │   └── UserServiceTest.java
        └── config/
            └── TestSecurityConfig.java
```

## Features

- **User Management** - CRUD operations for user entities
- **RESTful API** - Standard HTTP methods and status codes
- **Security** - Authentication and authorization
- **Error Handling** - Custom exceptions and meaningful error responses
- **Database** - JPA entities with H2 in-memory database
- **Testing** - Comprehensive unit tests for both service and controller layers

## Getting Started

### Prerequisites

- JDK 24 or later
- Maven 3.6 or later

### Installation

1. Clone the repository:
   ```bash
   git clone [repository-url]
   cd demo2-springboot
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```

### Running the Application

Run the application using Maven:

```bash
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

H2 Console can be accessed at `http://localhost:8080/h2-console` with these credentials:
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

## API Documentation

### User Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET    | /api/users | Get all users |
| GET    | /api/users?page={pageNumber}&size={pageSize} | Get paginated users |
| GET    | /api/users/{id} | Get user by ID |
| POST   | /api/users | Create a new user |
| PUT    | /api/users/{id} | Update an existing user |
| DELETE | /api/users/{id} | Delete a user |

Example request (Create User):
```http
POST /api/users
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "fullName": "John Doe",
  "active": true
}
```

### Pagination Support

The API supports pagination for retrieving users. To use pagination, include `page` and `size` query parameters:

```http
GET /api/users?page=0&size=10
```

Parameters:
- `page`: Zero-based page index (0 = first page, 1 = second page)
- `size`: Number of items per page

The response includes pagination metadata:
```json
{
  "content": [
    { 
      "id": 1,
      "username": "user1",
      // other user properties
    },
    // more users
  ],
  "pageable": {
    "sort": { "empty": true, "sorted": false, "unsorted": true },
    "offset": 0,
    "pageNumber": 0,
    "pageSize": 10,
    "paged": true,
    "unpaged": false
  },
  "last": false,
  "totalElements": 12,
  "totalPages": 2,
  "size": 10,
  "number": 0,
  "sort": { "empty": true, "sorted": false, "unsorted": true },
  "first": true,
  "numberOfElements": 10,
  "empty": false
}
```

## Testing

Run tests using Maven:

```bash
mvn test
```

The project includes:
- Unit tests for service layer logic
- Controller tests for API endpoints
- Security-related tests

## Security

The application is configured with Spring Security:
- API endpoints (/api/**) are permitted for all requests in the current configuration
- H2 console is accessible without authentication
- CSRF protection is disabled for API endpoints and H2 console

## Database Configuration

The application uses H2 in-memory database with the following configuration:
- URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`
- Hibernate DDL Auto: `update` (schema is automatically updated)
- SQL logging is enabled for debugging

JPA properties:
- Show SQL: `true`
- Format SQL: `true`
