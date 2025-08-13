# Test script for Healthcare Provider Management System APIs
# This script tests the Create Availability and View Appointment List APIs

Write-Host "Starting API Tests..." -ForegroundColor Green

# Base URL
$baseUrl = "http://localhost:8088"

# Test 1: Check if application is running
Write-Host "`n1. Testing if application is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/providers" -Method GET -UseBasicParsing
    Write-Host "✅ Application is running" -ForegroundColor Green
} catch {
    Write-Host "❌ Application is not running. Please start the application first." -ForegroundColor Red
    exit 1
}

# Test 2: Register a patient
Write-Host "`n2. Registering a patient..." -ForegroundColor Yellow
$patientData = @{
    first_name = "John"
    last_name = "Doe"
    email = "john.doe@example.com"
    password = "Password123!"
    confirm_password = "Password123!"
    phone_number = "+1234567890"
    date_of_birth = "1990-01-01"
    gender = "MALE"
    address = @{
        street = "123 Main St"
        city = "New York"
        state = "NY"
        zip = "10001"
    }
} | ConvertTo-Json -Depth 3

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/patient/register" -Method POST -Body $patientData -ContentType "application/json" -UseBasicParsing
    $patientResponse = $response.Content | ConvertFrom-Json
    $patientId = $patientResponse.data.patientId
    Write-Host "✅ Patient registered successfully. ID: $patientId" -ForegroundColor Green
} catch {
    Write-Host "❌ Patient registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 3: Register a provider
Write-Host "`n3. Registering a provider..." -ForegroundColor Yellow
$providerData = @{
    firstName = "Dr"
    lastName = "Smith"
    email = "dr.smith@example.com"
    password = "Password123!"
    phoneNumber = "+1987654321"
    licenseNumber = "MD123456"
    specialization = "CARDIOLOGY"
    yearsOfExperience = 10
    clinicAddress = @{
        street = "456 Medical Center Dr"
        city = "New York"
        state = "NY"
        zip = "10002"
    }
} | ConvertTo-Json -Depth 3

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/providers/register" -Method POST -Body $providerData -ContentType "application/json" -UseBasicParsing
    $providerResponse = $response.Content | ConvertFrom-Json
    $providerId = $providerResponse.id
    Write-Host "✅ Provider registered successfully. ID: $providerId" -ForegroundColor Green
} catch {
    Write-Host "❌ Provider registration failed: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# Test 4: Test Create Availability API
Write-Host "`n4. Testing Create Availability API..." -ForegroundColor Yellow
$availabilityData = @{
    date = "2025-08-20"
    start_time = "09:00"
    end_time = "17:00"
    appointment_type = "CONSULTATION"
    location = @{
        type = "CLINIC"
        address = "456 Medical Center Dr, New York, NY 10002"
        room_number = "101"
    }
    pricing = @{
        base_fee = 150.00
        insurance_accepted = $true
        currency = "USD"
    }
    timezone = "America/New_York"
    slot_duration = 30
    max_appointments_per_slot = 1
    break_duration = 15
    notes = "Regular consultation hours"
    is_recurring = $false
} | ConvertTo-Json -Depth 4

try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/v1/provider/availability?providerId=$providerId" -Method POST -Body $availabilityData -ContentType "application/json" -UseBasicParsing
    Write-Host "✅ Create Availability API is working!" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ Create Availability API failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
}

# Test 5: Test View Appointment List API
Write-Host "`n5. Testing View Appointment List API..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$baseUrl/api/appointments?page=0&size=10" -Method GET -UseBasicParsing
    Write-Host "✅ View Appointment List API is working!" -ForegroundColor Green
    Write-Host "Response: $($response.Content)" -ForegroundColor Cyan
} catch {
    Write-Host "❌ View Appointment List API failed: $($_.Exception.Message)" -ForegroundColor Red
    if ($_.Exception.Response) {
        $reader = New-Object System.IO.StreamReader($_.Exception.Response.GetResponseStream())
        $reader.BaseStream.Position = 0
        $responseBody = $reader.ReadToEnd()
        Write-Host "Response: $responseBody" -ForegroundColor Red
    }
}

Write-Host "`nAPI Testing completed!" -ForegroundColor Green
