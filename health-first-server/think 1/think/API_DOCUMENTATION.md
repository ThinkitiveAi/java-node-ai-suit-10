# Healthcare Application - Complete API Documentation

## Base URL
```
http://localhost:8088
```

## Authentication
Most endpoints require JWT token in Authorization header:
```
Authorization: Bearer <jwt_token>
```

---

## 1. PROVIDER APIS

### 1.1 Provider Registration (Public)
```bash
curl -X POST http://localhost:8088/api/providers/register \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Dr. Sarah",
    "lastName": "Johnson",
    "email": "sarah.johnson@healthcare.com",
    "password": "Password123!",
    "phoneNumber": "+1555999888",
    "specialization": "Dermatology",
    "licenseNumber": "MD789012",
    "yearsOfExperience": 8,
    "clinicAddress": {
      "street": "456 Health Ave",
      "city": "Boston",
      "state": "MA",
      "zip": "02101"
    }
  }'
```

**Response:**
```json
{
  "id": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
  "firstName": "Dr. Sarah",
  "lastName": "Johnson",
  "email": "sarah.johnson@healthcare.com",
  "phoneNumber": "+1555999888",
  "specialization": "Dermatology",
  "verificationStatus": "PENDING",
  "isActive": true
}
```

### 1.2 Provider Login (Public)
```bash
curl -X POST http://localhost:8088/api/v1/provider/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "sarah.johnson@healthcare.com",
    "password": "Password123!"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "expires_in": 3600,
    "token_type": "Bearer",
    "provider": {
      "id": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
      "firstName": "Dr. Sarah",
      "lastName": "Johnson",
      "specialization": "Dermatology"
    }
  }
}
```

### 1.3 Get All Providers (Provider Only)
```bash
curl -X GET http://localhost:8088/api/providers \
  -H "Authorization: Bearer <provider_token>"
```

### 1.4 Get Provider by ID (Provider Only)
```bash
curl -X GET http://localhost:8088/api/providers/5da702c5-a729-4d6e-a82c-0621e6a911e9 \
  -H "Authorization: Bearer <provider_token>"
```

---

## 2. PATIENT APIS

### 2.1 Patient Registration (Public)
```bash
curl -X POST http://localhost:8088/api/v1/patient/register \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Alice",
    "last_name": "Smith",
    "email": "alice.smith@email.com",
    "password": "Password123!",
    "confirm_password": "Password123!",
    "phone_number": "+1555777999",
    "date_of_birth": "1985-03-20",
    "gender": "FEMALE",
    "address": {
      "street": "789 Main St",
      "city": "Boston",
      "state": "MA",
      "zip": "02102"
    },
    "emergency_contact": {
      "name": "Bob Smith",
      "phone": "+1555888777",
      "relationship": "Spouse"
    },
    "medical_history": ["Allergies"],
    "insurance_info": {
      "provider": "HealthCare Plus",
      "policy_number": "HP123456"
    }
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Patient registered successfully. Verification email sent.",
  "data": {
    "patientId": "adcf56ec-6688-4eec-8f75-a955f8df3355",
    "email": "alice.smith@email.com",
    "phoneNumber": "+1555777999",
    "emailVerified": false,
    "phoneVerified": false
  }
}
```

### 2.2 Patient Login (Public)
```bash
curl -X POST http://localhost:8088/api/v1/patient/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "alice.smith@email.com",
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
      "patient_id": "adcf56ec-6688-4eec-8f75-a955f8df3355",
      "first_name": "Alice",
      "last_name": "Smith",
      "email": "alice.smith@email.com"
    },
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "expires_in": 1800,
    "token_type": "Bearer"
  }
}
```

---

## 3. AVAILABILITY APIS

### 3.1 Create Availability (Provider Only)
```bash
curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=5da702c5-a729-4d6e-a82c-0621e6a911e9" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <provider_token>" \
  -d '{
    "date": "2025-12-25",
    "startTime": "09:00",
    "endTime": "17:00",
    "timezone": "America/New_York",
    "slotDuration": 30,
    "breakDuration": 15,
    "appointmentType": "CONSULTATION",
    "location": {
      "type": "CLINIC",
      "address": "456 Health Ave, Boston, MA 02101",
      "roomNumber": "Room 101"
    },
    "pricing": {
      "baseFee": 200.00,
      "insuranceAccepted": true,
      "currency": "USD"
    },
    "notes": "Dermatology consultation slots"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Availability slots created successfully",
  "data": {
    "availability_id": "82e241ff-8013-4c38-9775-6e81c587bef7",
    "slots_created": 16,
    "date_range": {
      "start": "2025-12-25",
      "end": "2025-12-25"
    },
    "total_appointments_available": 16
  }
}
```

### 3.2 Get Provider Availability (Provider Only)
```bash
curl -X GET "http://localhost:8088/api/v1/provider/5da702c5-a729-4d6e-a82c-0621e6a911e9/availability?startDate=2025-12-01&endDate=2025-12-31" \
  -H "Authorization: Bearer <provider_token>"
```

### 3.3 Search Available Slots (Public)
```bash
curl -X GET "http://localhost:8088/api/v1/provider/availability/search?specialization=Dermatology&startDate=2025-12-01&endDate=2025-12-31&insuranceAccepted=true"
```

**Response:**
```json
{
  "success": true,
  "data": {
    "results": [
      {
        "provider": {
          "id": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
          "name": "Dr. Sarah Johnson",
          "specialization": "Dermatology",
          "rating": 4.5,
          "years_of_experience": 8,
          "clinic_address": "456 Health Ave, Boston, MA"
        },
        "available_slots": [
          {
            "slot_id": "slot-uuid",
            "date": "2025-12-25",
            "start_time": "09:00:00",
            "end_time": "09:30:00",
            "appointment_type": "CONSULTATION",
            "pricing": {
              "base_fee": 200.00,
              "currency": "USD",
              "insurance_accepted": true
            }
          }
        ]
      }
    ],
    "total_results": 1
  }
}
```

---

## 4. APPOINTMENT APIS

### 4.1 Book Appointment (Patient/Provider)
```bash
curl -X POST http://localhost:8088/api/appointments/book \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer <patient_or_provider_token>" \
  -d '{
    "patientId": "adcf56ec-6688-4eec-8f75-a955f8df3355",
    "providerId": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
    "appointmentDate": "2025-12-25",
    "appointmentTime": "10:00",
    "appointmentType": "CONSULTATION",
    "appointmentMode": "IN_PERSON",
    "reasonForVisit": "Skin consultation and checkup",
    "additionalNotes": "Patient has sensitive skin",
    "insuranceProvider": "HealthCare Plus",
    "insurancePolicyNumber": "HP123456"
  }'
```

**Response:**
```json
{
  "appointmentId": "4f6d7ce1-a3f0-4a3d-a399-78964b60eaed",
  "bookingReference": "APT-9671EFB3",
  "patientId": "adcf56ec-6688-4eec-8f75-a955f8df3355",
  "patientName": "Alice Smith",
  "providerId": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
  "providerName": "Dr. Sarah Johnson",
  "providerSpecialization": "Dermatology",
  "appointmentDate": "2025-12-25",
  "appointmentTime": "10:00:00",
  "appointmentType": "CONSULTATION",
  "appointmentMode": "IN_PERSON",
  "reasonForVisit": "Skin consultation and checkup",
  "status": "BOOKED",
  "estimatedCost": 200.00,
  "currency": "USD"
}
```

### 4.2 Get Appointments (Patient/Provider)
```bash
curl -X GET "http://localhost:8088/api/appointments?page=0&size=20&sortBy=appointmentDateTime&sortDirection=DESC" \
  -H "Authorization: Bearer <patient_or_provider_token>"
```

### 4.3 Get Appointment by Reference (Patient/Provider)
```bash
curl -X GET http://localhost:8088/api/appointments/APT-9671EFB3 \
  -H "Authorization: Bearer <patient_or_provider_token>"
```

### 4.4 Cancel Appointment (Patient/Provider)
```bash
curl -X PUT http://localhost:8088/api/appointments/APT-9671EFB3/cancel \
  -H "Authorization: Bearer <patient_or_provider_token>"
```

**Response:**
```json
{
  "appointmentId": "4f6d7ce1-a3f0-4a3d-a399-78964b60eaed",
  "bookingReference": "APT-9671EFB3",
  "status": "CANCELLED",
  "patientName": "Alice Smith",
  "providerName": "Dr. Sarah Johnson",
  "appointmentDate": "2025-12-25",
  "appointmentTime": "10:00:00"
}
```

---

## 5. SAMPLE TEST DATA

### Provider Data
```json
{
  "id": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
  "email": "sarah.johnson@healthcare.com",
  "password": "Password123!",
  "name": "Dr. Sarah Johnson",
  "specialization": "Dermatology",
  "phone": "+1555999888",
  "address": "456 Health Ave, Boston, MA 02101"
}
```

### Patient Data
```json
{
  "id": "adcf56ec-6688-4eec-8f75-a955f8df3355",
  "email": "alice.smith@email.com",
  "password": "Password123!",
  "name": "Alice Smith",
  "phone": "+1555777999",
  "address": "789 Main St, Boston, MA 02102"
}
```

### Appointment Data
```json
{
  "bookingReference": "APT-9671EFB3",
  "patientId": "adcf56ec-6688-4eec-8f75-a955f8df3355",
  "providerId": "5da702c5-a729-4d6e-a82c-0621e6a911e9",
  "date": "2025-12-25",
  "time": "10:00",
  "type": "CONSULTATION",
  "status": "BOOKED"
}
```

---

## 6. ERROR RESPONSES

### 401 Unauthorized
```json
{
  "timestamp": "2025-08-13T22:45:00.000+00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Full authentication is required to access this resource"
}
```

### 403 Forbidden
```json
{
  "timestamp": "2025-08-13T22:45:00.000+00:00",
  "status": 403,
  "error": "Forbidden",
  "message": "Access Denied"
}
```

### 400 Bad Request
```json
{
  "success": false,
  "message": "Validation failed",
  "errors": {
    "email": "Email must be a valid email address",
    "password": "Password must be at least 8 characters"
  }
}
```

---

## 7. AUTHENTICATION FLOW

1. **Register** → Get account
2. **Login** → Get JWT token
3. **Use token** → Access protected endpoints
4. **Token expires** → Login again

### Token Expiration
- **Provider tokens**: 1 hour (3600 seconds)
- **Patient tokens**: 30 minutes (1800 seconds)