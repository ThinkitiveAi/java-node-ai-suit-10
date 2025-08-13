#!/bin/bash

BASE_URL="http://localhost:8089"
echo "Testing Provider Availability Management API"
echo "============================================"

# First, register a provider to get a provider ID
echo -e "\n1. Registering a provider for testing..."
PROVIDER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/providers/register" \
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
  }')

echo "Provider registration response: $PROVIDER_RESPONSE"

# Extract provider ID from response (you may need to adjust this based on actual response format)
PROVIDER_ID=$(echo $PROVIDER_RESPONSE | grep -o '"id":"[^"]*"' | cut -d'"' -f4)
echo "Provider ID: $PROVIDER_ID"

if [ -z "$PROVIDER_ID" ]; then
    echo "Failed to get provider ID. Using a test ID..."
    PROVIDER_ID="test-provider-123"
fi

echo -e "\n2. Creating availability slots..."
curl -X POST "$BASE_URL/api/v1/provider/availability?providerId=$PROVIDER_ID" \
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
    "recurrence_end_date": "2024-03-15",
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
    "notes": "Standard consultation slots"
  }'

echo -e "\n\n3. Getting provider availability..."
curl -X GET "$BASE_URL/api/v1/provider/$PROVIDER_ID/availability?startDate=2024-02-15&endDate=2024-02-20"

echo -e "\n\n4. Searching available slots..."
curl -X GET "$BASE_URL/api/v1/provider/availability/search?specialization=Cardiology&date=2024-02-15&insurance_accepted=true&max_price=200.00"

echo -e "\n\n5. Creating another availability for testing updates..."
UPDATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/provider/availability?providerId=$PROVIDER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "date": "2024-02-16",
    "start_time": "10:00",
    "end_time": "16:00",
    "timezone": "America/New_York",
    "slot_duration": 45,
    "break_duration": 0,
    "is_recurring": false,
    "appointment_type": "FOLLOW_UP",
    "location": {
      "type": "CLINIC",
      "address": "123 Medical Center Dr, New York, NY 10001",
      "room_number": "Room 206"
    },
    "pricing": {
      "base_fee": 200.00,
      "insurance_accepted": true,
      "currency": "USD"
    },
    "notes": "Follow-up consultation slots"
  }')

echo "Update availability response: $UPDATE_RESPONSE"

# Extract slot ID for testing updates (you may need to adjust this)
SLOT_ID=$(echo $UPDATE_RESPONSE | grep -o '"slot_id":"[^"]*"' | cut -d'"' -f4 | head -1)
if [ -z "$SLOT_ID" ]; then
    echo "Using test slot ID..."
    SLOT_ID="test-slot-123"
fi

echo -e "\n\n6. Updating availability slot..."
curl -X PUT "$BASE_URL/api/v1/provider/availability/$SLOT_ID?providerId=$PROVIDER_ID" \
  -H "Content-Type: application/json" \
  -d '{
    "start_time": "2024-02-16T11:00:00",
    "end_time": "2024-02-16T11:45:00",
    "status": "AVAILABLE",
    "notes": "Updated consultation time"
  }'

echo -e "\n\n7. Testing search with different criteria..."
curl -X GET "$BASE_URL/api/v1/provider/availability/search?start_date=2024-02-15&end_date=2024-02-20&appointment_type=CONSULTATION&location=New York"

echo -e "\n\n8. Testing search with insurance filter..."
curl -X GET "$BASE_URL/api/v1/provider/availability/search?insurance_accepted=true&max_price=150.00"

echo -e "\n\n9. Testing search with timezone..."
curl -X GET "$BASE_URL/api/v1/provider/availability/search?timezone=America/New_York&available_only=true"

echo -e "\n\n10. Testing error cases..."

echo -e "\n10.1. Creating availability with overlapping time..."
curl -X POST "$BASE_URL/api/v1/provider/availability?providerId=$PROVIDER_ID" \
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

echo -e "\n10.2. Creating availability with invalid time range..."
curl -X POST "$BASE_URL/api/v1/provider/availability?providerId=$PROVIDER_ID" \
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

echo -e "\n10.3. Getting availability for non-existent provider..."
curl -X GET "$BASE_URL/api/v1/provider/non-existent-id/availability?startDate=2024-02-15&endDate=2024-02-20"

echo -e "\n\nProvider Availability API testing completed!"
echo "================================================"
echo "Note: Some tests may fail if the provider ID or slot ID extraction doesn't work correctly."
echo "You can manually adjust the IDs in the script based on the actual responses."
