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
- **Service** - Business logic and security services
- **Controller** - REST API endpoints
- **Exception** - Custom exceptions and error handling
- **Config** - Application configuration classes (Security, etc.)
- **Test** - Unit and integration tests
- **DTOs** - Data Transfer Objects for authentication and other operations
- **Security** - JWT authentication filter and security configurations

```
src/
├── main/
│   ├── java/com/cc/data/demo2springboot/
│   │   ├── Demo2SpringbootApplication.java
│   │   ├── config/
│   │   │   └── SecurityConfig.java
│   │   ├── controller/
│   │   │   ├── UserController.java
│   │   │   └── AuthController.java
│   │   ├── dto/
│   │   │   ├── AuthRequest.java
│   │   │   ├── AuthResponse.java
│   │   │   └── UserDto.java
│   │   ├── exception/
│   │   │   └── ResourceNotFoundException.java
│   │   ├── filter/
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── model/
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── UserRepository.java
│   │   └── service/
│   │       ├── UserService.java
│   │       └── JwtService.java
│   └── resources/
│       ├── application.properties
│       ├── openapi.yaml
│       └── user-requests.http
└── test/
    └── java/com/cc/data/demo2springboot/
        ├── controller/
        │   ├── AuthControllerTest.java
        │   ├── UserControllerTest.java
        │   └── UserControllerSecurityTest.java
        ├── service/
        │   └── UserServiceTest.java
        │   └── JwtServiceTest.java
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
# Set the JWT secret environment variable before running the application
export JWT_SECRET=3F4428472B4B6250655368566D5971337336763979244226452948404D635166
mvn spring-boot:run
```

The application will be available at `http://localhost:8080`.

H2 Console can be accessed at `http://localhost:8080/h2-console` with these credentials:

- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### Environment Variables

For security reasons, sensitive configuration is stored in environment variables rather than in properties files:

| Variable     | Description                              | Default                                                     |
| ------------ | ---------------------------------------- | ----------------------------------------------------------- |
| JWT_SECRET   | Secret key used for JWT token generation | A hardcoded development value (not secure for production)   |
| DB_USERNAME  | Username for H2 database connection      | sa                                                          |
| DB_PASSWORD  | Password for H2 database connection      | password                                                    |

You can set these environment variables in your development environment:

```bash
# macOS/Linux
export JWT_SECRET=your_secure_secret_key
export DB_USERNAME=your_db_username
export DB_PASSWORD=your_secure_password

# Windows Command Prompt
set JWT_SECRET=your_secure_secret_key
set DB_USERNAME=your_db_username
set DB_PASSWORD=your_secure_password

# Windows PowerShell
$env:JWT_SECRET="your_secure_secret_key"
$env:DB_USERNAME="your_db_username"
$env:DB_PASSWORD="your_secure_password"
```

For production, ensure these variables are securely set in your deployment environment.

## API Documentation

### Authentication Endpoints

| Method | Endpoint        | Description             | Auth Required |
| ------ | --------------- | ----------------------- | ------------- |
| POST   | /api/auth/login | Login and get JWT token | No            |

Example authentication request:

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin"
}
```

Example response:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IkFETUlOLFVTRVIiLCJzdWIiOiJhZG1pbiIsImlhdCI6MTYzMDc2MjQ4NSwiZXhwIjoxNjMwODQ4ODg1fQ.example-token"
}
```

### User Endpoints

| Method | Endpoint                                     | Description             | Auth Required    |
| ------ | -------------------------------------------- | ----------------------- | ---------------- |
| GET    | /api/users                                   | Get all users           | No               |
| GET    | /api/users?page={pageNumber}&size={pageSize} | Get paginated users     | No               |
| GET    | /api/users/{id}                              | Get user by ID          | No               |
| POST   | /api/users                                   | Create a new user       | Yes (ADMIN role) |
| PUT    | /api/users/{id}                              | Update an existing user | Yes (ADMIN role) |
| DELETE | /api/users/{id}                              | Delete a user           | Yes (ADMIN role) |

### Authentication Flow

The application uses JWT (JSON Web Token) for authentication:

1. **Login to get a token** - Send credentials to `/api/auth/login`
2. **Store the token** - Save the returned JWT token
3. **Use the token for protected endpoints** - Include the token in the Authorization header

#### How to Use JWT Authentication

1. **Step 1: Login to get a JWT token**

   ```http
   POST http://localhost:8080/api/auth/login
   Content-Type: application/json

   {
     "username": "admin",
     "password": "admin"
   }
   ```

2. **Step 2: Save the token from the response**

   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IkFETUlOLFVTRVIiLCJzdWIiOiJhZG1pbiIsImlhdCI..."
   }
   ```

3. **Step 3: Use token for protected endpoints** (POST, PUT, DELETE operations)

   ```http
   POST http://localhost:8080/api/users
   Content-Type: application/json
   Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlcyI6IkFETUlOLFVTRVIiLCJzdWIiOiJhZG1p...

   {
     "username": "johndoe",
     "email": "john@example.com",
     "fullName": "John Doe",
     "active": true
   }
   ```

#### Available Test Users

The application comes with two pre-configured users:

1. **Regular User**

   - Username: `user`
   - Password: `password`
   - Role: `USER`
   - Permissions: Can view users but cannot modify

2. **Admin User**
   - Username: `admin`
   - Password: `admin`
   - Roles: `USER, ADMIN`
   - Permissions: Full access (create, read, update, delete)

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
      "username": "user1"
      // other user properties
    }
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
- Security tests for authentication and authorization

### Security Testing

The application includes comprehensive security tests in `UserControllerSecurityTest.java` that verify:

- Authentication requirements for different endpoints
- Authorization based on user roles (USER vs ADMIN)
- CSRF protection for state-changing operations
- Proper HTTP method handling
- Rate limiting behavior
- JWT token authentication

These tests ensure that:

1. Anonymous users can view users but cannot create, update, or delete
2. Regular users (ROLE_USER) can view but have limited write access
3. Admin users (ROLE_ADMIN) have full access to all endpoints
4. All endpoints are properly protected against CSRF attacks
5. The application handles OPTIONS requests for CORS preflight correctly

Security tests are organized into nested test classes for better readability:

- AuthenticationTests
- AuthorizationTests
- CsrfTests
- MethodSecurityTests
- RateLimitingTests
- TokenAuthenticationTests

## Security

The application is configured with Spring Security:

- API endpoints (/api/\*\*) are permitted for all requests in the current configuration
- H2 console is accessible without authentication
- CSRF protection is disabled for API endpoints and H2 console

## Database Configuration

The application uses H2 in-memory database with the following configuration:

- URL: `jdbc:h2:mem:testdb`
- Username: Environment variable `DB_USERNAME` (defaults to `sa` if not set)
- Password: Environment variable `DB_PASSWORD` (defaults to `password` if not set)
- Hibernate DDL Auto: `update` (schema is automatically updated)
- SQL logging is enabled for debugging

### Setting Database Credentials

Database credentials can be configured via environment variables for enhanced security:

```bash
# Set database credentials before running the application
export DB_USERNAME=custom_username
export DB_PASSWORD=secure_password
mvn spring-boot:run
```

For local development, the default values will be used if environment variables are not set. In production environments, always set these variables with secure values.

### Database Configuration for Testing

For unit tests, the application uses a test-specific configuration that doesn't rely on environment variables. This ensures consistent test execution regardless of the local environment setup.

JPA properties:

- Show SQL: `true`
- Format SQL: `true`
