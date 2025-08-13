# Healthcare Application API Testing Guide

## Overview
This guide provides comprehensive testing examples for all working APIs in the healthcare application.

## Base URL
```
http://localhost:8088
```

## 1. Provider Management APIs

### 1.1 Provider Registration
**Endpoint**: `POST /api/providers/register`

**Request Body**:
```json
{
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
}
```

**PowerShell Example**:
```powershell
$body = @{
    firstName = "Dr. John"
    lastName = "Smith"
    email = "john.smith@healthcare.com"
    password = "Password123!"
    phoneNumber = "+1234567890"
    specialization = "Cardiology"
    licenseNumber = "MD123456"
    yearsOfExperience = 10
    clinicAddress = @{
        street = "123 Medical Center Dr"
        city = "New York"
        state = "NY"
        zip = "10001"
    }
} | ConvertTo-Json -Depth 3

Invoke-RestMethod -Uri "http://localhost:8088/api/providers/register" -Method POST -Body $body -ContentType "application/json"
```

**Expected Response**:
```json
{
  "id": "uuid-here",
  "firstName": "Dr. John",
  "lastName": "Smith",
  "email": "john.smith@healthcare.com",
  "phoneNumber": "+1234567890",
  "specialization": "Cardiology",
  "licenseNumber": "MD123456",
  "yearsOfExperience": 10,
  "clinicAddress": {
    "street": "123 Medical Center Dr",
    "city": "New York",
    "state": "NY",
    "zip": "10001"
  },
  "verificationStatus": "PENDING",
  "isActive": true,
  "createdAt": "2025-08-13T19:25:45.559176",
  "updatedAt": "2025-08-13T19:25:45.560202"
}
```

### 1.2 Provider Login
**Endpoint**: `POST /api/v1/provider/login`

**Request Body**:
```json
{
  "email": "john.smith@healthcare.com",
  "password": "Password123!"
}
```

**PowerShell Example**:
```powershell
$body = @{
    email = "john.smith@healthcare.com"
    password = "Password123!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8088/api/v1/provider/login" -Method POST -Body $body -ContentType "application/json"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "access_token": "eyJhbGciOiJIUzI1NiJ9...",
    "provider": {
      "id": "uuid-here",
      "firstName": "Dr. John",
      "lastName": "Smith",
      "email": "john.smith@healthcare.com"
    }
  }
}
```

### 1.3 Get All Providers
**Endpoint**: `GET /api/providers`

**PowerShell Example**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/providers" -Method GET
```

**Query Parameters**:
- `activeOnly` (boolean, default: false)
- `specialization` (string, optional)

**Example with filters**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/providers?activeOnly=true&specialization=Cardiology" -Method GET
```

### 1.4 Get Provider by ID
**Endpoint**: `GET /api/providers/{id}`

**PowerShell Example**:
```powershell
$providerId = "e5f2b26b-fefa-41c5-9d56-edd6fb1435c5"
Invoke-RestMethod -Uri "http://localhost:8088/api/providers/$providerId" -Method GET
```

### 1.5 Get Provider by Email
**Endpoint**: `GET /api/providers/email/{email}`

**PowerShell Example**:
```powershell
$email = "john.smith@healthcare.com"
Invoke-RestMethod -Uri "http://localhost:8088/api/providers/email/$email" -Method GET
```

## 2. Patient Management APIs

### 2.1 Patient Registration
**Endpoint**: `POST /api/v1/patient/register`

**Request Body**:
```json
{
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
  "medical_history": ["Hypertension", "Diabetes"],
  "insurance_info": {
    "provider": "Blue Cross",
    "policy_number": "BC123456"
  }
}
```

**PowerShell Example**:
```powershell
$body = @{
    first_name = "Jane"
    last_name = "Doe"
    email = "jane.doe@email.com"
    password = "Password123!"
    confirm_password = "Password123!"
    phone_number = "+1987654321"
    date_of_birth = "1990-01-15"
    gender = "FEMALE"
    address = @{
        street = "456 Oak Street"
        city = "Los Angeles"
        state = "CA"
        zip = "90210"
    }
    emergency_contact = @{
        name = "John Doe"
        phone = "+1555123456"
        relationship = "Spouse"
    }
    medical_history = @("Hypertension", "Diabetes")
    insurance_info = @{
        provider = "Blue Cross"
        policy_number = "BC123456"
    }
} | ConvertTo-Json -Depth 4

Invoke-RestMethod -Uri "http://localhost:8088/api/v1/patient/register" -Method POST -Body $body -ContentType "application/json"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Patient registered successfully. Verification email sent.",
  "data": {
    "patientId": "uuid-here",
    "email": "jane.doe@email.com",
    "phoneNumber": "+1987654321",
    "emailVerified": false,
    "phoneVerified": false
  }
}
```

### 2.2 Patient Login
**Endpoint**: `POST /api/v1/patient/login`

**Request Body**:
```json
{
  "email": "jane.doe@email.com",
  "password": "Password123!"
}
```

**PowerShell Example**:
```powershell
$body = @{
    email = "jane.doe@email.com"
    password = "Password123!"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8088/api/v1/patient/login" -Method POST -Body $body -ContentType "application/json"
```

**Expected Response**:
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "patient": {
      "id": "uuid-here",
      "firstName": "Jane",
      "lastName": "Doe",
      "email": "jane.doe@email.com"
    },
    "access_token": "eyJhbGciOiJIUzI1NiJ9..."
  }
}
```

## 3. Appointment Management APIs

### 3.1 Get Appointment List
**Endpoint**: `GET /api/appointments`

**PowerShell Example**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/appointments" -Method GET
```

**Query Parameters**:
- `startDate` (YYYY-MM-DD, optional)
- `endDate` (YYYY-MM-DD, optional)
- `appointmentType` (string, optional)
- `providerId` (UUID, optional)
- `patientId` (UUID, optional)
- `status` (string, optional)
- `page` (integer, default: 0)
- `size` (integer, default: 20)
- `sortBy` (string, default: "appointmentDateTime")
- `sortDirection` (string, default: "DESC")

**Example with filters**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/appointments?startDate=2025-01-01&endDate=2025-12-31&appointmentType=CONSULTATION" -Method GET
```

**Expected Response**:
```json
{
  "appointments": [],
  "currentPage": 0,
  "totalPages": 0,
  "totalElements": 0,
  "pageSize": 20,
  "hasNext": false,
  "hasPrevious": false
}
```

### 3.2 Book Appointment
**Endpoint**: `POST /api/appointments/book`

**Request Body**:
```json
{
  "patientId": "patient-uuid",
  "providerId": "provider-uuid",
  "appointmentDate": "2025-12-25",
  "appointmentTime": "10:00",
  "appointmentType": "CONSULTATION",
  "appointmentMode": "IN_PERSON",
  "reasonForVisit": "Regular checkup and consultation",
  "additionalNotes": "Patient has sensitive skin",
  "insuranceProvider": "Blue Cross",
  "insurancePolicyNumber": "BC123456"
}
```

**PowerShell Example**:
```powershell
$body = @{
    patientId = "411d4f97-009f-40c0-a780-fb5810f11d4b"
    providerId = "e5f2b26b-fefa-41c5-9d56-edd6fb1435c5"
    appointmentDate = "2025-12-25"
    appointmentTime = "10:00"
    appointmentType = "CONSULTATION"
    appointmentMode = "IN_PERSON"
    reasonForVisit = "Regular checkup and consultation"
    additionalNotes = "Patient has sensitive skin"
    insuranceProvider = "Blue Cross"
    insurancePolicyNumber = "BC123456"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8088/api/appointments/book" -Method POST -Body $body -ContentType "application/json"
```

**Expected Response** (when no availability exists):
```json
{
  "error": "No available slot found for the requested time"
}
```

### 3.3 Get Appointment by Booking Reference
**Endpoint**: `GET /api/appointments/{bookingReference}`

**PowerShell Example**:
```powershell
$bookingRef = "APT-12345678"
Invoke-RestMethod -Uri "http://localhost:8088/api/appointments/$bookingRef" -Method GET
```

### 3.4 Cancel Appointment
**Endpoint**: `PUT /api/appointments/{bookingReference}/cancel`

**PowerShell Example**:
```powershell
$bookingRef = "APT-12345678"
Invoke-RestMethod -Uri "http://localhost:8088/api/appointments/$bookingRef/cancel" -Method PUT
```

## 4. Availability Management APIs

### 4.1 Search Available Slots
**Endpoint**: `GET /api/v1/provider/availability/search`

**PowerShell Example**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/v1/provider/availability/search?specialization=Neurology&location=Boston" -Method GET
```

**Query Parameters**:
- `date` (YYYY-MM-DD, optional)
- `startDate` (YYYY-MM-DD, optional)
- `endDate` (YYYY-MM-DD, optional)
- `specialization` (string, optional)
- `location` (string, optional)
- `appointmentType` (string, optional)
- `insuranceAccepted` (boolean, optional)
- `maxPrice` (number, optional)
- `timezone` (string, optional)
- `availableOnly` (boolean, default: true)

**Example with multiple filters**:
```powershell
Invoke-RestMethod -Uri "http://localhost:8088/api/v1/provider/availability/search?specialization=Neurology&location=Boston&startDate=2025-12-01&endDate=2025-12-31&insuranceAccepted=true&maxPrice=300" -Method GET
```

**Expected Response**:
```json
{
  "success": true,
  "data": {
    "results": [],
    "search_criteria": {
      "specialization": "Neurology",
      "location": "Boston"
    },
    "total_results": 0
  }
}
```

### 4.2 Get Provider Availability
**Endpoint**: `GET /api/v1/provider/{providerId}/availability`

**PowerShell Example**:
```powershell
$providerId = "e5f2b26b-fefa-41c5-9d56-edd6fb1435c5"
Invoke-RestMethod -Uri "http://localhost:8088/api/v1/provider/$providerId/availability?startDate=2025-12-01&endDate=2025-12-31" -Method GET
```

**Query Parameters**:
- `startDate` (YYYY-MM-DD, required)
- `endDate` (YYYY-MM-DD, required)
- `status` (string, optional)
- `appointmentType` (string, optional)

## 5. Error Handling Examples

### 5.1 Validation Error (Provider Registration)
**Request** (missing required fields):
```json
{
  "firstName": "Dr. John",
  "email": "invalid-email"
}
```

**Response**:
```json
{
  "email": "Email must be a valid email address",
  "clinicAddress": "Clinic address is required",
  "password": "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
}
```

### 5.2 Authentication Error (Provider Login)
**Request** (wrong password):
```json
{
  "email": "john.smith@healthcare.com",
  "password": "wrongpassword"
}
```

**Response**:
```json
{
  "error": "Invalid email or password"
}
```

### 5.3 Not Found Error (Provider by ID)
**Request**: `GET /api/providers/non-existent-uuid`

**Response**:
```json
{
  "error": "Provider not found"
}
```

## 6. Testing Scripts

### 6.1 Complete Provider Workflow
```powershell
# 1. Register Provider
$providerBody = @{
    firstName = "Dr. Test"
    lastName = "Provider"
    email = "test.provider@healthcare.com"
    password = "Password123!"
    phoneNumber = "+1555123456"
    specialization = "General Practice"
    licenseNumber = "MD999999"
    yearsOfExperience = 5
    clinicAddress = @{
        street = "999 Test Street"
        city = "Test City"
        state = "TS"
        zip = "12345"
    }
} | ConvertTo-Json -Depth 3

$provider = Invoke-RestMethod -Uri "http://localhost:8088/api/providers/register" -Method POST -Body $providerBody -ContentType "application/json"
Write-Host "Provider registered with ID: $($provider.id)"

# 2. Login Provider
$loginBody = @{
    email = "test.provider@healthcare.com"
    password = "Password123!"
} | ConvertTo-Json

$loginResponse = Invoke-RestMethod -Uri "http://localhost:8088/api/v1/provider/login" -Method POST -Body $loginBody -ContentType "application/json"
Write-Host "Provider logged in successfully"

# 3. Get Provider Details
$providerDetails = Invoke-RestMethod -Uri "http://localhost:8088/api/providers/$($provider.id)" -Method GET
Write-Host "Provider details retrieved: $($providerDetails.firstName) $($providerDetails.lastName)"
```

### 6.2 Complete Patient Workflow
```powershell
# 1. Register Patient
$patientBody = @{
    first_name = "Test"
    last_name = "Patient"
    email = "test.patient@email.com"
    password = "Password123!"
    confirm_password = "Password123!"
    phone_number = "+1555987654"
    date_of_birth = "1985-06-15"
    gender = "MALE"
    address = @{
        street = "888 Patient Ave"
        city = "Patient City"
        state = "PC"
        zip = "54321"
    }
    emergency_contact = @{
        name = "Emergency Contact"
        phone = "+1555111111"
        relationship = "Spouse"
    }
    medical_history = @("None")
    insurance_info = @{
        provider = "Test Insurance"
        policy_number = "TI123456"
    }
} | ConvertTo-Json -Depth 4

$patient = Invoke-RestMethod -Uri "http://localhost:8088/api/v1/patient/register" -Method POST -Body $patientBody -ContentType "application/json"
Write-Host "Patient registered with ID: $($patient.data.patientId)"

# 2. Login Patient
$patientLoginBody = @{
    email = "test.patient@email.com"
    password = "Password123!"
} | ConvertTo-Json

$patientLoginResponse = Invoke-RestMethod -Uri "http://localhost:8088/api/v1/patient/login" -Method POST -Body $patientLoginBody -ContentType "application/json"
Write-Host "Patient logged in successfully"
```

## 7. Notes

1. **Data Persistence**: The application now uses a file-based H2 database, so data persists across application restarts.

2. **Availability Creation**: Currently not working due to an internal server error. This prevents appointment booking.

3. **Security**: JWT tokens are generated but not yet used for API protection.

4. **Error Handling**: Most APIs return appropriate error messages, but some return generic "unexpected error" messages.

5. **Validation**: All APIs include comprehensive input validation with detailed error messages.

## 8. Troubleshooting

### Common Issues:
1. **Application not starting**: Check if port 8088 is available
2. **Database connection issues**: Check if the data directory is writable
3. **Validation errors**: Review the request body against the DTO requirements
4. **Availability creation fails**: This is a known issue that needs debugging

### Debug Steps:
1. Check application logs for detailed error messages
2. Verify database schema using H2 console (http://localhost:8088/h2-console)
3. Test with minimal request bodies to isolate issues
4. Check entity relationships and foreign key constraints
