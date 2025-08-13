# Provider Availability Management API - cURL Commands

This document provides comprehensive cURL commands for testing the Provider Availability Management API endpoints.

## Base Information
- **Base URL**: `http://localhost:8089`
- **Content-Type**: `application/json`
- **Swagger UI**: `http://localhost:8089/swagger-ui.html`

## API Endpoints Overview

### 1. Create Availability Slots
**POST** `/api/v1/provider/availability`

### 2. Get Provider Availability
**GET** `/api/v1/provider/{providerId}/availability`

### 3. Update Availability Slot
**PUT** `/api/v1/provider/availability/{slotId}`

### 4. Delete Availability Slot
**DELETE** `/api/v1/provider/availability/{slotId}`

### 5. Search Available Slots
**GET** `/api/v1/provider/availability/search`

---

## 1. Create Availability Slots

### 1.1 Basic Availability Creation

```bash
curl -X POST "http://localhost:8089/api/v1/provider/availability?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-02-15",
    "start_time": "09:00",
    "end_time": "17:00",
    "timezone": "America/New_York",
    "slot_duration": 30,
    "break_duration": 15,
    "is_recurring": false,
    "appointment_type": "CONSULTATION",
    "location": {
      "type": "CLINIC",
      "address": "123 Medical Center Dr, New York, NY 10001",
      "room_number": "Room 205"
    },
    "pricing": {
      "base_fee": 150.00,
      "insurance_accepted": true,
      "currency": "USD"
    },
    "notes": "Standard consultation slots"
  }'
```

**Expected Response (201 Created)**:
```json
{
  "success": true,
  "message": "Availability slots created successfully",
  "data": {
    "availability_id": "uuid-here",
    "slots_created": 16,
    "date_range": {
      "start": "2024-02-15",
      "end": "2024-02-15"
    },
    "total_appointments_available": 16
  }
}
```

### 1.2 Recurring Availability Creation

```bash
curl -X POST "http://localhost:8089/api/v1/provider/availability?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-02-15",
    "start_time": "09:00",
    "end_time": "17:00",
    "timezone": "America/New_York",
    "slot_duration": 30,
    "break_duration": 15,
    "is_recurring": true,
    "recurrence_pattern": "WEEKLY",
    "recurrence_end_date": "2024-08-15",
    "appointment_type": "CONSULTATION",
    "location": {
      "type": "CLINIC",
      "address": "123 Medical Center Dr, New York, NY 10001",
      "room_number": "Room 205"
    },
    "pricing": {
      "base_fee": 150.00,
      "insurance_accepted": true,
      "currency": "USD"
    },
    "special_requirements": ["fasting_required", "bring_insurance_card"],
    "notes": "Weekly consultation slots"
  }'
```

### 1.3 Telemedicine Availability

```bash
curl -X POST "http://localhost:8089/api/v1/provider/availability?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-02-15",
    "start_time": "10:00",
    "end_time": "16:00",
    "timezone": "America/New_York",
    "slot_duration": 45,
    "break_duration": 0,
    "is_recurring": false,
    "appointment_type": "TELEMEDICINE",
    "location": {
      "type": "TELEMEDICINE",
      "address": "Virtual consultation"
    },
    "pricing": {
      "base_fee": 120.00,
      "insurance_accepted": true,
      "currency": "USD"
    },
    "notes": "Telemedicine consultation slots"
  }'
```

---

## 2. Get Provider Availability

### 2.1 Basic Availability Retrieval

```bash
curl -X GET "http://localhost:8089/api/v1/provider/provider-uuid-123/availability?startDate=2024-02-15&endDate=2024-02-20"
```

**Expected Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "provider_id": "provider-uuid-123",
    "availability_summary": {
      "total_slots": 48,
      "available_slots": 32,
      "booked_slots": 14,
      "cancelled_slots": 2
    },
    "availability": [
      {
        "date": "2024-02-15",
        "slots": [
          {
            "slot_id": "slot-uuid-here",
            "start_time": "09:00",
            "end_time": "09:30",
            "status": "AVAILABLE",
            "appointment_type": "CONSULTATION",
            "location": {
              "type": "CLINIC",
              "address": "123 Medical Center Dr",
              "room_number": "Room 205"
            },
            "pricing": {
              "base_fee": 150.00,
              "insurance_accepted": true
            }
          }
        ]
      }
    ]
  }
}
```

### 2.2 Filtered Availability Retrieval

```bash
# Filter by status
curl -X GET "http://localhost:8089/api/v1/provider/provider-uuid-123/availability?startDate=2024-02-15&endDate=2024-02-20&status=AVAILABLE"

# Filter by appointment type
curl -X GET "http://localhost:8089/api/v1/provider/provider-uuid-123/availability?startDate=2024-02-15&endDate=2024-02-20&appointmentType=CONSULTATION"

# Filter by both status and appointment type
curl -X GET "http://localhost:8089/api/v1/provider/provider-uuid-123/availability?startDate=2024-02-15&endDate=2024-02-20&status=AVAILABLE&appointmentType=CONSULTATION"
```

---

## 3. Update Availability Slot

### 3.1 Update Slot Time

```bash
curl -X PUT "http://localhost:8089/api/v1/provider/availability/slot-uuid-123?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "start_time": "2024-02-15T10:00:00",
    "end_time": "2024-02-15T10:30:00",
    "status": "AVAILABLE",
    "notes": "Updated consultation time"
  }'
```

### 3.2 Update Slot Status

```bash
curl -X PUT "http://localhost:8089/api/v1/provider/availability/slot-uuid-123?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "status": "BLOCKED",
    "notes": "Emergency maintenance"
  }'
```

### 3.3 Update Pricing

```bash
curl -X PUT "http://localhost:8089/api/v1/provider/availability/slot-uuid-123?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "pricing": {
      "base_fee": 175.00,
      "insurance_accepted": true,
      "currency": "USD"
    }
  }'
```

---

## 4. Delete Availability Slot

### 4.1 Delete Single Slot

```bash
curl -X DELETE "http://localhost:8089/api/v1/provider/availability/slot-uuid-123?providerId=provider-uuid-123"
```

### 4.2 Delete Recurring Slots

```bash
curl -X DELETE "http://localhost:8089/api/v1/provider/availability/slot-uuid-123?providerId=provider-uuid-123&deleteRecurring=true&reason=Schedule change"
```

---

## 5. Search Available Slots

### 5.1 Basic Search

```bash
curl -X GET "http://localhost:8089/api/v1/provider/availability/search?specialization=Cardiology&date=2024-02-15"
```

**Expected Response (200 OK)**:
```json
{
  "success": true,
  "data": {
    "search_criteria": {
      "date": "2024-02-15",
      "specialization": "Cardiology"
    },
    "total_results": 15,
    "results": [
      {
        "provider": {
          "id": "provider-uuid-here",
          "name": "Dr. John Doe",
          "specialization": "Cardiology",
          "years_of_experience": 15,
          "rating": 4.8,
          "clinic_address": "123 Medical Center Dr, New York, NY"
        },
        "available_slots": [
          {
            "slot_id": "slot-uuid-here",
            "date": "2024-02-15",
            "start_time": "10:00",
            "end_time": "10:30",
            "appointment_type": "CONSULTATION",
            "location": {
              "type": "CLINIC",
              "address": "123 Medical Center Dr",
              "room_number": "Room 205"
            },
            "pricing": {
              "base_fee": 150.00,
              "insurance_accepted": true,
              "currency": "USD"
            },
            "special_requirements": ["bring_insurance_card"]
          }
        ]
      }
    ]
  }
}
```

### 5.2 Advanced Search with Multiple Filters

```bash
curl -X GET "http://localhost:8089/api/v1/provider/availability/search?start_date=2024-02-15&end_date=2024-02-20&specialization=Cardiology&appointment_type=CONSULTATION&insurance_accepted=true&max_price=200.00&timezone=America/New_York&available_only=true"
```

### 5.3 Search by Location

```bash
curl -X GET "http://localhost:8089/api/v1/provider/availability/search?location=New York&date=2024-02-15"
```

### 5.4 Search by Insurance and Price

```bash
curl -X GET "http://localhost:8089/api/v1/provider/availability/search?insurance_accepted=true&max_price=150.00&date=2024-02-15"
```

---

## Error Scenarios

### 1. Overlapping Time Slots

```bash
curl -X POST "http://localhost:8089/api/v1/provider/availability?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-02-15",
    "start_time": "10:00",
    "end_time": "12:00",
    "timezone": "America/New_York",
    "slot_duration": 30,
    "appointment_type": "CONSULTATION",
    "location": {
      "type": "CLINIC",
      "address": "123 Medical Center Dr, New York, NY 10001"
    }
  }'
```

**Expected Response (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Time slot overlaps with existing availability"
}
```

### 2. Invalid Time Range

```bash
curl -X POST "http://localhost:8089/api/v1/provider/availability?providerId=provider-uuid-123" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-02-15",
    "start_time": "17:00",
    "end_time": "09:00",
    "timezone": "America/New_York",
    "slot_duration": 30,
    "appointment_type": "CONSULTATION",
    "location": {
      "type": "CLINIC",
      "address": "123 Medical Center Dr, New York, NY 10001"
    }
  }'
```

**Expected Response (400 Bad Request)**:
```json
{
  "success": false,
  "message": "End time must be after start time"
}
```

### 3. Provider Not Found

```bash
curl -X GET "http://localhost:8089/api/v1/provider/non-existent-id/availability?startDate=2024-02-15&endDate=2024-02-20"
```

**Expected Response (400 Bad Request)**:
```json
{
  "success": false,
  "message": "Provider not found"
}
```

---

## Testing Scripts

### Linux/Mac
```bash
chmod +x test-provider-availability.sh
./test-provider-availability.sh
```

### Windows
```batch
test-provider-availability.bat
```

---

## Swagger Documentation

Access the interactive API documentation at:
- **Swagger UI**: `http://localhost:8089/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8089/v3/api-docs`

---

## Key Features

### 1. Time Zone Handling
- All times stored in UTC in database
- Automatic conversion to provider's local timezone
- Daylight saving time support

### 2. Conflict Prevention
- Overlapping slot detection
- Minimum and maximum slot duration validation
- Existing appointment checks before deletion

### 3. Recurring Patterns
- Daily, weekly, monthly recurrence
- Flexible end dates
- Automatic slot generation

### 4. Advanced Search
- Multiple filter criteria
- Insurance and pricing filters
- Location-based search
- Specialization filtering

### 5. Security Features
- Provider ownership validation
- Input sanitization
- Comprehensive validation
- Error handling

---

## Database Schema

### ProviderAvailability Entity
- **id**: UUID (Primary Key)
- **provider_id**: UUID (Foreign Key to Provider)
- **date**: LocalDate
- **start_time**: LocalTime
- **end_time**: LocalTime
- **timezone**: String
- **is_recurring**: Boolean
- **recurrence_pattern**: Enum (DAILY, WEEKLY, MONTHLY)
- **recurrence_end_date**: LocalDate
- **slot_duration**: Integer (minutes)
- **break_duration**: Integer (minutes)
- **status**: Enum (AVAILABLE, BOOKED, CANCELLED, BLOCKED, MAINTENANCE)
- **max_appointments_per_slot**: Integer
- **current_appointments**: Integer
- **appointment_type**: Enum (CONSULTATION, FOLLOW_UP, EMERGENCY, TELEMEDICINE)
- **location**: Embedded object
- **pricing**: Embedded object
- **notes**: String
- **special_requirements**: List<String>
- **created_at**: LocalDateTime
- **updated_at**: LocalDateTime

### AppointmentSlot Entity
- **id**: UUID (Primary Key)
- **availability_id**: UUID (Foreign Key)
- **provider_id**: UUID (Foreign Key)
- **slot_start_time**: LocalDateTime
- **slot_end_time**: LocalDateTime
- **status**: Enum (AVAILABLE, BOOKED, CANCELLED, BLOCKED)
- **patient_id**: UUID (Foreign Key, nullable)
- **appointment_type**: String
- **booking_reference**: String (unique)
- **created_at**: LocalDateTime
- **updated_at**: LocalDateTime

---

## HTTP Status Codes

- **200 OK**: Successful retrieval/update
- **201 Created**: Availability slots created successfully
- **400 Bad Request**: Invalid request data or business logic errors
- **404 Not Found**: Provider or slot not found
- **422 Unprocessable Entity**: Validation errors
- **500 Internal Server Error**: Server-side errors

---

## Prerequisites

Before testing the availability API, ensure you have:

1. **Registered a provider** using the provider registration endpoint
2. **Application is running** on port 8089
3. **Database is accessible** and contains provider data
4. **Swagger UI is accessible** for interactive testing

### Example: Register a provider first

```bash
curl -X POST "http://localhost:8089/api/providers/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "Dr. John",
    "lastName": "Cardiologist",
    "email": "john.cardiologist@clinic.com",
    "phoneNumber": "+1234567890",
    "password": "SecurePassword123!",
    "specialization": "Cardiology",
    "licenseNumber": "CARD123456",
    "yearsOfExperience": 15,
    "clinicAddress": {
      "street": "123 Medical Center Dr",
      "city": "New York",
      "state": "NY",
      "zip": "10001"
    }
  }'
```

Then use the returned provider ID in the availability API calls.
