#!/bin/bash

BASE_URL="http://localhost:8089"
API_ENDPOINT="/api/v1/patient/login"

echo "Testing Patient Login API"
echo "========================="

# Test 1: Successful login
echo -e "\n1. Testing successful login..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com",
    "password": "SecurePassword123!"
  }'
echo -e "\n"

# Test 2: Invalid email
echo -e "\n2. Testing invalid email..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@email.com",
    "password": "SecurePassword123!"
  }'
echo -e "\n"

# Test 3: Invalid password
echo -e "\n3. Testing invalid password..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com",
    "password": "WrongPassword123!"
  }'
echo -e "\n"

# Test 4: Invalid email format
echo -e "\n4. Testing invalid email format..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "invalid-email",
    "password": "SecurePassword123!"
  }'
echo -e "\n"

# Test 5: Missing email
echo -e "\n5. Testing missing email..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "password": "SecurePassword123!"
  }'
echo -e "\n"

# Test 6: Missing password
echo -e "\n6. Testing missing password..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com"
  }'
echo -e "\n"

# Test 7: Empty email
echo -e "\n7. Testing empty email..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "",
    "password": "SecurePassword123!"
  }'
echo -e "\n"

# Test 8: Empty password
echo -e "\n8. Testing empty password..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "jane.smith@email.com",
    "password": ""
  }'
echo -e "\n"

# Test 9: Login with different registered patient
echo -e "\n9. Testing login with different registered patient..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@email.com",
    "password": "SecurePassword123!"
  }'
echo -e "\n"

# Test 10: Case insensitive email
echo -e "\n10. Testing case insensitive email..."
curl -X POST "$BASE_URL$API_ENDPOINT" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "JANE.SMITH@EMAIL.COM",
    "password": "SecurePassword123!"
  }'
echo -e "\n"

echo "Patient Login API testing completed!"
