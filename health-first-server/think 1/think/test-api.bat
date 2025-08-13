@echo off
REM Healthcare Provider API Test Script for Windows
REM This script tests the provider registration and login functionality

set BASE_URL=http://localhost:8080

echo 🏥 Healthcare Provider API Test Script
echo ======================================
echo.

REM Test 1: Register a new provider
echo 📝 Test 1: Registering a new provider...
echo ----------------------------------------

curl -s -X POST "%BASE_URL%/api/providers/register" ^
  -H "Content-Type: application/json" ^
  -d "{\"firstName\": \"John\", \"lastName\": \"Doe\", \"email\": \"john.doe@clinic.com\", \"phoneNumber\": \"+1234567890\", \"password\": \"SecurePassword123!\", \"specialization\": \"Cardiology\", \"licenseNumber\": \"MD123456\", \"yearsOfExperience\": 10, \"clinicAddress\": {\"street\": \"123 Medical Center Dr\", \"city\": \"New York\", \"state\": \"NY\", \"zip\": \"10001\"}}"

echo.
echo.

REM Test 2: Login with the registered provider
echo 🔐 Test 2: Logging in with the registered provider...
echo -----------------------------------------------------

curl -s -X POST "%BASE_URL%/api/v1/provider/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"john.doe@clinic.com\", \"password\": \"SecurePassword123!\"}"

echo.
echo.

REM Test 3: Test login with invalid credentials
echo 🚫 Test 3: Testing login with invalid credentials...
echo ---------------------------------------------------

curl -s -X POST "%BASE_URL%/api/v1/provider/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"john.doe@clinic.com\", \"password\": \"WrongPassword123!\"}"

echo.
echo.

REM Test 4: Test login with non-existent email
echo 🚫 Test 4: Testing login with non-existent email...
echo --------------------------------------------------

curl -s -X POST "%BASE_URL%/api/v1/provider/login" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"nonexistent@clinic.com\", \"password\": \"SecurePassword123!\"}"

echo.
echo.

echo 🏁 API Testing Complete!
echo ========================
pause
