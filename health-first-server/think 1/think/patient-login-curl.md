# Patient Login API - cURL Commands

This document provides cURL commands for testing the Patient Login API endpoint.

## Base Information
- **Endpoint**: `POST /api/v1/patient/login`
- **Base URL**: `http://localhost:8089`
- **Content-Type**: `application/json`

## 1. Successful Login

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com",
    "password": "SecurePassword123!"
  }'
```

**Expected Response (200 OK)**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "expires_in": 1800,
    "token_type": "Bearer",
    "patient": {
      "patient_id": "patient-uuid-here",
      "email": "jane.smith@email.com",
      "first_name": "Jane",
      "last_name": "Smith",
      "phone_number": "+1234567890",
      "email_verified": true,
      "phone_verified": false,
      "is_active": true
    }
  }
}
```

## 2. Error Scenarios

### 2.1 Invalid Email (401 Unauthorized)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@email.com",
    "password": "SecurePassword123!"
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

### 2.2 Invalid Password (401 Unauthorized)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com",
    "password": "WrongPassword123!"
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Invalid email or password"
}
```

### 2.3 Inactive Account (401 Unauthorized)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "inactive@email.com",
    "password": "SecurePassword123!"
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Account is deactivated. Please contact support."
}
```

## 3. Validation Errors

### 3.1 Invalid Email Format (400 Bad Request)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "SecurePassword123!"
  }'
```

### 3.2 Missing Email (400 Bad Request)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "SecurePassword123!"
  }'
```

### 3.3 Missing Password (400 Bad Request)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com"
  }'
```

### 3.4 Empty Email (400 Bad Request)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "",
    "password": "SecurePassword123!"
  }'
```

### 3.5 Empty Password (400 Bad Request)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com",
    "password": ""
  }'
```

## 4. Advanced Scenarios

### 4.1 Case Insensitive Email

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "JANE.SMITH@EMAIL.COM",
    "password": "SecurePassword123!"
  }'
```

### 4.2 Email with Whitespace

```bash
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "  jane.smith@email.com  ",
    "password": "SecurePassword123!"
  }'
```

## 5. Using the JWT Token

After successful login, you can use the JWT token for authenticated requests:

```bash
# Store the token in a variable (Linux/Mac)
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

# Use the token in subsequent requests
curl -X GET "http://localhost:8089/api/v1/patient/profile" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json"
```

For Windows (PowerShell):
```powershell
$TOKEN = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."

curl -X GET "http://localhost:8089/api/v1/patient/profile" `
  -H "Authorization: Bearer $TOKEN" `
  -H "Content-Type: application/json"
```

## 6. Testing Scripts

You can also use the provided test scripts:
- **Linux/Mac**: `./test-patient-login.sh`
- **Windows**: `test-patient-login.bat`

## 7. JWT Token Details

- **Token Type**: Bearer
- **Expiry**: 30 minutes (1800 seconds)
- **Payload Fields**:
  - `patient_id`: Patient's unique identifier
  - `email`: Patient's email address
  - `role`: Always "PATIENT"
  - `exp`: Expiration timestamp
  - `iat`: Issued at timestamp

## 8. Security Features

1. **Password Hashing**: Passwords are verified using bcrypt with 12 salt rounds
2. **Account Status Check**: Only active accounts can log in
3. **Case Insensitive**: Email addresses are case-insensitive
4. **Input Sanitization**: Email and password are trimmed of whitespace
5. **JWT Security**: Tokens are signed and have expiration times
6. **Generic Error Messages**: Invalid credentials return the same error message for security

## 9. HTTP Status Codes

- **200 OK**: Successful login
- **400 Bad Request**: Validation errors (invalid email format, missing fields)
- **401 Unauthorized**: Invalid credentials or inactive account
- **500 Internal Server Error**: Server-side errors

## 10. Prerequisites

Before testing the login API, make sure you have:

1. **Registered a patient** using the registration endpoint
2. **Application is running** on port 8089
3. **Database is accessible** and contains patient data

### Example: Register a patient first

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Jane",
    "last_name": "Smith",
    "email": "jane.smith@email.com",
    "phone_number": "+1234567890",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1990-05-15",
    "gender": "FEMALE",
    "address": {
      "street": "456 Main Street",
      "city": "Boston",
      "state": "MA",
      "zip": "02101"
    }
  }'
```

Then test the login with the same credentials.
