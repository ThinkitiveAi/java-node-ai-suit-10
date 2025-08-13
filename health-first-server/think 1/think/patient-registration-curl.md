# Patient Registration API - cURL Commands

This document provides cURL commands for testing the Patient Registration API endpoint.

## Base Information
- **Endpoint**: `POST /api/v1/patient/register`
- **Base URL**: `http://localhost:8089`
- **Content-Type**: `application/json`

## 1. Successful Registration (All Fields)

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
    },
    "emergency_contact": {
      "name": "John Smith",
      "phone": "+1234567891",
      "relationship": "spouse"
    },
    "insurance_info": {
      "provider": "Blue Cross",
      "policy_number": "BC123456789"
    },
    "medical_history": ["Hypertension", "Diabetes"]
  }'
```

**Expected Response (201 Created)**:
```json
{
  "success": true,
  "message": "Patient registered successfully. Verification email sent.",
  "data": {
    "patient_id": "uuid-here",
    "email": "jane.smith@email.com",
    "phone_number": "+1234567890",
    "email_verified": false,
    "phone_verified": false
  }
}
```

## 2. Minimal Registration (Required Fields Only)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "John",
    "last_name": "Doe",
    "email": "john.doe@email.com",
    "phone_number": "+1234567892",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1985-10-20",
    "gender": "MALE",
    "address": {
      "street": "123 Oak Avenue",
      "city": "Chicago",
      "state": "IL",
      "zip": "60601"
    }
  }'
```

## 3. Error Scenarios

### 3.1 Duplicate Email (422 Unprocessable Entity)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Jane",
    "last_name": "Doe",
    "email": "jane.smith@email.com",
    "phone_number": "+1234567893",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1992-03-10",
    "gender": "FEMALE",
    "address": {
      "street": "789 Pine Street",
      "city": "Miami",
      "state": "FL",
      "zip": "33101"
    }
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Email is already registered",
  "errors": {
    "email": "Email is already registered"
  }
}
```

### 3.2 Password Mismatch (422 Unprocessable Entity)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Alice",
    "last_name": "Johnson",
    "email": "alice.johnson@email.com",
    "phone_number": "+1234567894",
    "password": "SecurePassword123!",
    "confirm_password": "DifferentPassword123!",
    "date_of_birth": "1988-07-22",
    "gender": "FEMALE",
    "address": {
      "street": "654 Maple Drive",
      "city": "Denver",
      "state": "CO",
      "zip": "80201"
    }
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Password confirmation does not match",
  "errors": {
    "confirmPassword": "Password confirmation does not match"
  }
}
```

### 3.3 Underage Patient (422 Unprocessable Entity)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Young",
    "last_name": "Patient",
    "email": "young.patient@email.com",
    "phone_number": "+1234567895",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "2015-01-01",
    "gender": "OTHER",
    "address": {
      "street": "987 Cedar Lane",
      "city": "Phoenix",
      "state": "AZ",
      "zip": "85001"
    }
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Patient must be at least 13 years old",
  "errors": {
    "dateOfBirth": "Patient must be at least 13 years old"
  }
}
```

### 3.4 Weak Password (422 Unprocessable Entity)

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Weak",
    "last_name": "Password",
    "email": "weak.password@email.com",
    "phone_number": "+1234567896",
    "password": "weak",
    "confirm_password": "weak",
    "date_of_birth": "1980-06-15",
    "gender": "MALE",
    "address": {
      "street": "147 Birch Road",
      "city": "Atlanta",
      "state": "GA",
      "zip": "30301"
    }
  }'
```

**Expected Response**:
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "password": "Password must contain at least 8 characters including uppercase, lowercase, number, and special character"
  }
}
```

## 4. Field Validation Errors

### 4.1 Invalid Email Format

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Invalid",
    "last_name": "Email",
    "email": "invalid-email",
    "phone_number": "+1234567897",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1975-09-30",
    "gender": "FEMALE",
    "address": {
      "street": "258 Spruce Street",
      "city": "Portland",
      "state": "OR",
      "zip": "97201"
    }
  }'
```

### 4.2 Invalid Phone Format

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Invalid",
    "last_name": "Phone",
    "email": "invalid.phone@email.com",
    "phone_number": "1234567890",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1982-04-12",
    "gender": "MALE",
    "address": {
      "street": "369 Willow Way",
      "city": "Las Vegas",
      "state": "NV",
      "zip": "89101"
    }
  }'
```

### 4.3 Invalid ZIP Code Format

```bash
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Invalid",
    "last_name": "Zip",
    "email": "invalid.zip@email.com",
    "phone_number": "+1234567898",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1970-11-08",
    "gender": "FEMALE",
    "address": {
      "street": "741 Poplar Street",
      "city": "San Diego",
      "state": "CA",
      "zip": "invalid"
    }
  }'
```

## 5. Testing Scripts

You can also use the provided test scripts:
- **Linux/Mac**: `./test-patient-api.sh`
- **Windows**: `test-patient-api.bat`

## 6. Important Notes

1. **Field Naming**: All JSON fields use snake_case format (e.g., `first_name`, `phone_number`, `date_of_birth`)
2. **Password Requirements**: Must contain at least 8 characters with uppercase, lowercase, number, and special character
3. **Age Requirement**: Patients must be at least 13 years old (COPPA compliance)
4. **Phone Format**: Must be in international format starting with `+` (e.g., `+1234567890`)
5. **ZIP Code Format**: Must be 5 digits or 5+4 format (e.g., `12345` or `12345-6789`)
6. **Gender Options**: `MALE`, `FEMALE`, `OTHER`, `PREFER_NOT_TO_SAY`

## 7. HTTP Status Codes

- **201 Created**: Successful registration
- **422 Unprocessable Entity**: Validation errors or business logic violations
- **400 Bad Request**: Malformed JSON or missing required headers
- **500 Internal Server Error**: Server-side errors
