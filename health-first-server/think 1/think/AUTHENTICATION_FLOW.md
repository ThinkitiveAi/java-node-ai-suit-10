# Healthcare Application Authentication Flow

## Overview
The healthcare application now implements JWT-based authentication with role-based access control for Providers and Patients.

## Authentication Flow

### 1. Provider Authentication Flow

#### Step 1: Provider Registration
```bash
curl -X POST http://localhost:8088/api/providers/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Dr. John",
    "lastName": "Smith",
    "email": "john.smith@healthcare.com",
    "password": "Password123!",
    "phoneNumber": "+1234567890",
    "specialization": "Cardiology",
    "licenseNumber": "MD123456",
    "yearsOfExperience": 10,
    "clinicAddress": {
      "street": "123 Medical Center Dr",
      "city": "New York",
      "state": "NY",
      "zip": "10001"
    }
  }'
```

**Response:**
```json
{
  "id": "b0368def-2ae4-4b4d-be0e-e06b1b3a7cad",
  "firstName": "Dr. John",
  "lastName": "Smith",
  "email": "john.smith@healthcare.com",
  "verificationStatus": "PENDING",
  "isActive": true
}
```

#### Step 2: Provider Login
```bash
curl -X POST http://localhost:8088/api/v1/provider/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.smith@healthcare.com",
    "password": "Password123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUFJPVklERVIi...",
    "expires_in": 3600,
    "token_type": "Bearer",
    "provider": {
      "id": "b0368def-2ae4-4b4d-be0e-e06b1b3a7cad",
      "firstName": "Dr. John",
      "lastName": "Smith",
      "specialization": "Cardiology"
    }
  }
}
```

#### Step 3: Using Provider Token for Protected Endpoints
```bash
# Create Availability (Provider-only endpoint)
curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=b0368def-2ae4-4b4d-be0e-e06b1b3a7cad" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUFJPVklERVIi..." \
  -d '{
    "date": "2025-12-25",
    "startTime": "09:00",
    "endTime": "17:00",
    "timezone": "America/New_York",
    "location": {
      "type": "CLINIC",
      "address": "123 Medical Center Dr, New York, NY 10001"
    }
  }'
```

### 2. Patient Authentication Flow

#### Step 1: Patient Registration
```bash
curl -X POST http://localhost:8088/api/v1/patient/register \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Jane",
    "last_name": "Doe",
    "email": "jane.doe@email.com",
    "password": "Password123!",
    "confirm_password": "Password123!",
    "phone_number": "+1987654321",
    "date_of_birth": "1990-01-15",
    "gender": "FEMALE",
    "address": {
      "street": "456 Oak Street",
      "city": "Los Angeles",
      "state": "CA",
      "zip": "90210"
    },
    "emergency_contact": {
      "name": "John Doe",
      "phone": "+1555123456",
      "relationship": "Spouse"
    },
    "medical_history": ["Hypertension"],
    "insurance_info": {
      "provider": "Blue Cross",
      "policy_number": "BC123456"
    }
  }'
```

#### Step 2: Patient Login
```bash
curl -X POST http://localhost:8088/api/v1/patient/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.doe@email.com",
    "password": "Password123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "patient": {
      "id": "24857f3a-8ce2-46ad-a020-504fbe9dadf3",
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane.doe@email.com"
    },
    "access_token": "eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEFUSUVOVCI...",
    "expires_in": 1800,
    "token_type": "Bearer"
  }
}
```

#### Step 3: Using Patient Token for Protected Endpoints
```bash
# Book Appointment (Patient and Provider can access)
curl -X POST http://localhost:8088/api/appointments/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiUEFUSUVOVCI..." \
  -d '{
    "patientId": "24857f3a-8ce2-46ad-a020-504fbe9dadf3",
    "providerId": "b0368def-2ae4-4b4d-be0e-e06b1b3a7cad",
    "appointmentDate": "2025-12-25",
    "appointmentTime": "10:00",
    "appointmentType": "CONSULTATION",
    "appointmentMode": "IN_PERSON",
    "reasonForVisit": "Regular checkup and consultation"
  }'
```

## API Endpoint Security Configuration

### Public Endpoints (No Authentication Required)
- `POST /api/providers/register` - Provider registration
- `POST /api/v1/provider/login` - Provider login
- `POST /api/v1/patient/register` - Patient registration
- `POST /api/v1/patient/login` - Patient login
- `GET /h2-console/**` - Database console (development only)
- `GET /swagger-ui/**` - API documentation
- `GET /v3/api-docs/**` - OpenAPI specification

### Provider-Only Endpoints (ROLE_PROVIDER Required)
- `POST /api/v1/provider/availability` - Create availability slots
- `GET /api/v1/provider/{providerId}/availability` - Get provider availability
- `PUT /api/v1/provider/availability/{slotId}` - Update availability slot
- `DELETE /api/v1/provider/availability/{slotId}` - Delete availability slot
- `GET /api/providers/**` - Provider management endpoints

### Patient and Provider Endpoints (ROLE_PATIENT or ROLE_PROVIDER Required)
- `POST /api/appointments/book` - Book appointment
- `GET /api/appointments` - List appointments
- `GET /api/appointments/{bookingReference}` - Get appointment details
- `PUT /api/appointments/{bookingReference}/cancel` - Cancel appointment

### Public Search Endpoints (No Authentication Required)
- `GET /api/v1/provider/availability/search` - Search available slots

## JWT Token Structure

### Provider Token Claims
```json
{
  "role": "PROVIDER",
  "provider_id": "b0368def-2ae4-4b4d-be0e-e06b1b3a7cad",
  "specialization": "Cardiology",
  "email": "john.smith@healthcare.com",
  "sub": "john.smith@healthcare.com",
  "iat": 1755101503,
  "exp": 1755105103
}
```

### Patient Token Claims
```json
{
  "role": "PATIENT",
  "patient_id": "24857f3a-8ce2-46ad-a020-504fbe9dadf3",
  "email": "jane.doe@email.com",
  "sub": "jane.doe@email.com",
  "iat": 1755101628,
  "exp": 1755103428
}
```

## Error Responses

### 401 Unauthorized (Missing or Invalid Token)
```json
{
  "timestamp": "2025-08-13T16:00:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource",
  "path": "/api/v1/provider/availability"
}
```

### 403 Forbidden (Insufficient Permissions)
```json
{
  "timestamp": "2025-08-13T16:00:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied",
  "path": "/api/v1/provider/availability"
}
```

## Security Features Implemented

1. **JWT-based Authentication**: Stateless authentication using JSON Web Tokens
2. **Role-based Access Control**: Different permissions for Providers and Patients
3. **Token Expiration**: Providers get 1-hour tokens, Patients get 30-minute tokens
4. **CORS Configuration**: Properly configured for cross-origin requests
5. **Password Encryption**: BCrypt with strength 12 for password hashing
6. **Input Validation**: Comprehensive validation on all endpoints
7. **SQL Injection Prevention**: JPA/Hibernate with parameterized queries

## Testing Authentication

To test the authentication after application restart:

1. **Test without token** (should return 401):
```bash
curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=test" \
  -H "Content-Type: application/json" \
  -d '{"date": "2025-12-25"}'
```

2. **Test with invalid token** (should return 401):
```bash
curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=test" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer invalid-token" \
  -d '{"date": "2025-12-25"}'
```

3. **Test with patient token on provider endpoint** (should return 403):
```bash
curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=test" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <patient-token>" \
  -d '{"date": "2025-12-25"}'
```

4. **Test with valid provider token** (should work):
```bash
curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=test" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <provider-token>" \
  -d '{"date": "2025-12-25"}'
```