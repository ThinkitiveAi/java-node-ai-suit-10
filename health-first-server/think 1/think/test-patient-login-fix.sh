#!/bin/bash

echo "Testing Patient Login Fix"
echo "========================="

echo -e "\n1. First, register a patient..."
curl -X POST "http://localhost:8089/api/v1/patient/register" \
  -H "Content-Type: application/json" \
  -d '{
    "first_name": "Test",
    "last_name": "Patient",
    "email": "test.patient@email.com",
    "phone_number": "+1234567899",
    "password": "SecurePassword123!",
    "confirm_password": "SecurePassword123!",
    "date_of_birth": "1990-01-01",
    "gender": "MALE",
    "address": {
      "street": "123 Test Street",
      "city": "Test City",
      "state": "TS",
      "zip": "12345"
    }
  }'

echo -e "\n\n2. Now test the login..."
curl -X POST "http://localhost:8089/api/v1/patient/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "test.patient@email.com",
    "password": "SecurePassword123!"
  }'

echo -e "\n\nTest completed!"
