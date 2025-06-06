# User API Requests

This file contains HTTP requests to test the User API endpoints defined in UserController.

## Prerequisites
- The Spring Boot application should be running on localhost:8080
- You can use these examples with tools like cURL, Postman, HTTPie, or any other HTTP client

## GET All Users
Retrieves a list of all users.

```bash
curl -X GET http://localhost:8080/api/users \
     -H "Content-Type: application/json"
```

## GET User by ID
Retrieves a specific user by their ID.

```bash
# Replace 1 with an actual user ID
curl -X GET http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json"
```

## POST Create a New User
Creates a new user in the system.

```bash
curl -X POST http://localhost:8080/api/users \
     -H "Content-Type: application/json" \
     -d '{
        "username": "johndoe",
        "email": "john.doe@example.com",
        "fullName": "John Doe",
        "active": true
     }'
```

## PUT Update an Existing User
Updates an existing user with new information.

```bash
# Replace 1 with an actual user ID
curl -X PUT http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json" \
     -d '{
        "username": "johndoe_updated",
        "email": "john.updated@example.com",
        "fullName": "John Doe Updated",
        "active": false
     }'
```

## DELETE a User
Deletes a user from the system.

```bash
# Replace 1 with an actual user ID
curl -X DELETE http://localhost:8080/api/users/1 \
     -H "Content-Type: application/json"
```

## Alternative Format for Postman or Other HTTP Clients

### GET All Users
- Method: GET
- URL: http://localhost:8080/api/users
- Headers: Content-Type: application/json

### GET User by ID
- Method: GET
- URL: http://localhost:8080/api/users/1
- Headers: Content-Type: application/json

### POST Create User
- Method: POST
- URL: http://localhost:8080/api/users
- Headers: Content-Type: application/json
- Body (raw JSON):
```json
{
  "username": "johndoe",
  "email": "john.doe@example.com",
  "fullName": "John Doe",
  "active": true
}
```

### PUT Update User
- Method: PUT
- URL: http://localhost:8080/api/users/1
- Headers: Content-Type: application/json
- Body (raw JSON):
```json
{
  "username": "johndoe_updated",
  "email": "john.updated@example.com",
  "fullName": "John Doe Updated",
  "active": false
}
```

### DELETE User
- Method: DELETE
- URL: http://localhost:8080/api/users/1
- Headers: Content-Type: application/json
