#!/bin/bash

# Healthcare Provider API Test Script
# This script tests the provider registration and login functionality

BASE_URL="http://localhost:8080"

echo "🏥 Healthcare Provider API Test Script"
echo "======================================"
echo ""

# Test 1: Register a new provider
echo "📝 Test 1: Registering a new provider..."
echo "----------------------------------------"

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/api/providers/register" \
  -H "Content-Type: application/json" \
  -d '{
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@clinic.com",
    "phoneNumber": "+1234567890",
    "password": "SecurePassword123!",
    "specialization": "Cardiology",
    "licenseNumber": "MD123456",
    "yearsOfExperience": 10,
    "clinicAddress": {
      "street": "123 Medical Center Dr",
      "city": "New York",
      "state": "NY",
      "zip": "10001"
    }
  }')

echo "Registration Response:"
echo "$REGISTER_RESPONSE" | jq '.' 2>/dev/null || echo "$REGISTER_RESPONSE"
echo ""

# Extract provider ID from registration response (if successful)
PROVIDER_ID=$(echo "$REGISTER_RESPONSE" | jq -r '.id' 2>/dev/null)
if [ "$PROVIDER_ID" != "null" ] && [ "$PROVIDER_ID" != "" ]; then
    echo "✅ Provider registered successfully with ID: $PROVIDER_ID"
    echo ""
    
    # Test 2: Login with the registered provider
    echo "🔐 Test 2: Logging in with the registered provider..."
    echo "-----------------------------------------------------"
    
    LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/provider/login" \
      -H "Content-Type: application/json" \
      -d '{
        "email": "john.doe@clinic.com",
        "password": "SecurePassword123!"
      }')
    
    echo "Login Response:"
    echo "$LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$LOGIN_RESPONSE"
    echo ""
    
    # Extract JWT token from login response
    JWT_TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.access_token' 2>/dev/null)
    if [ "$JWT_TOKEN" != "null" ] && [ "$JWT_TOKEN" != "" ]; then
        echo "✅ Login successful! JWT Token received."
        echo "Token: ${JWT_TOKEN:0:50}..."
        echo ""
        
        # Test 3: Get provider details using the JWT token
        echo "👤 Test 3: Getting provider details with JWT token..."
        echo "----------------------------------------------------"
        
        PROVIDER_RESPONSE=$(curl -s -X GET "$BASE_URL/api/providers/$PROVIDER_ID" \
          -H "Authorization: Bearer $JWT_TOKEN")
        
        echo "Provider Details Response:"
        echo "$PROVIDER_RESPONSE" | jq '.' 2>/dev/null || echo "$PROVIDER_RESPONSE"
        echo ""
        
    else
        echo "❌ Login failed!"
    fi
    
else
    echo "❌ Provider registration failed!"
fi

# Test 4: Test login with invalid credentials
echo "🚫 Test 4: Testing login with invalid credentials..."
echo "---------------------------------------------------"

INVALID_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/provider/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "john.doe@clinic.com",
    "password": "WrongPassword123!"
  }')

echo "Invalid Login Response:"
echo "$INVALID_LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$INVALID_LOGIN_RESPONSE"
echo ""

# Test 5: Test login with non-existent email
echo "🚫 Test 5: Testing login with non-existent email..."
echo "----------------------------------------------------"

NONEXISTENT_LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/provider/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "nonexistent@clinic.com",
    "password": "SecurePassword123!"
  }')

echo "Non-existent Email Login Response:"
echo "$NONEXISTENT_LOGIN_RESPONSE" | jq '.' 2>/dev/null || echo "$NONEXISTENT_LOGIN_RESPONSE"
echo ""

echo "🏁 API Testing Complete!"
echo "========================"
