#!/bin/bash

BASE_URL="http://localhost:8089"
API_ENDPOINT="/api/v1/patient/register"

echo "Testing Patient Registration API"
echo "================================="

# Test 1: Successful registration with all fields
echo -e "\n1. Testing successful registration with all fields..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 2: Minimal registration (required fields only)
echo -e "\n2. Testing minimal registration (required fields only)..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 3: Duplicate email
echo -e "\n3. Testing duplicate email..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 4: Duplicate phone number
echo -e "\n4. Testing duplicate phone number..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Bob",
    "last_name": "Wilson",
    "email": "bob.wilson@email.com",
    "phone_number": "+1234567890",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1978-12-05",
    "gender": "MALE",
    "address": {
      "street": "321 Elm Street",
      "city": "Seattle",
      "state": "WA",
      "zip": "98101"
    }
  }'
echo -e "\n"

# Test 5: Password mismatch
echo -e "\n5. Testing password mismatch..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 6: Underage patient (less than 13 years old)
echo -e "\n6. Testing underage patient (less than 13 years old)..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 7: Weak password
echo -e "\n7. Testing weak password..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 8: Invalid email format
echo -e "\n8. Testing invalid email format..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 9: Invalid phone format
echo -e "\n9. Testing invalid phone format..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

# Test 10: Invalid ZIP code format
echo -e "\n10. Testing invalid ZIP code format..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
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
echo -e "\n"

echo "Patient Registration API testing completed!"
