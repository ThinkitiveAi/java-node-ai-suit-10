@echo off
setlocal

set BASE_URL=http://localhost:8089
set API_ENDPOINT=/api/v1/patient/login

echo Testing Patient Login API
echo =========================

REM Test 1: Successful login
echo.
echo 1. Testing successful login...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"jane.smith@email.com\", \"password\": \"SecurePassword123!\"}"
echo.

REM Test 2: Invalid email
echo.
echo 2. Testing invalid email...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"nonexistent@email.com\", \"password\": \"SecurePassword123!\"}"
echo.

REM Test 3: Invalid password
echo.
echo 3. Testing invalid password...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"jane.smith@email.com\", \"password\": \"WrongPassword123!\"}"
echo.

REM Test 4: Invalid email format
echo.
echo 4. Testing invalid email format...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"invalid-email\", \"password\": \"SecurePassword123!\"}"
echo.

REM Test 5: Missing email
echo.
echo 5. Testing missing email...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"password\": \"SecurePassword123!\"}"
echo.

REM Test 6: Missing password
echo.
echo 6. Testing missing password...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"jane.smith@email.com\"}"
echo.

REM Test 7: Empty email
echo.
echo 7. Testing empty email...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"\", \"password\": \"SecurePassword123!\"}"
echo.

REM Test 8: Empty password
echo.
echo 8. Testing empty password...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"jane.smith@email.com\", \"password\": \"\"}"
echo.

REM Test 9: Login with different registered patient
echo.
echo 9. Testing login with different registered patient...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"john.doe@email.com\", \"password\": \"SecurePassword123!\"}"
echo.

REM Test 10: Case insensitive email
echo.
echo 10. Testing case insensitive email...
curl -X POST "%BASE_URL%%API_ENDPOINT%" ^
  -H "Content-Type: application/json" ^
  -d "{\"email\": \"JANE.SMITH@EMAIL.COM\", \"password\": \"SecurePassword123!\"}"
echo.

echo.
echo Patient Login API testing completed!
pause
