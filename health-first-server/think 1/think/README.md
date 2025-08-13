# Healthcare Provider & Patient Registration System

A secure Spring Boot application for healthcare provider and patient registration with comprehensive validation, security features, and testing.

## Features

- **Secure Provider Registration**: Complete registration system with validation
- **Provider Login**: JWT-based authentication for healthcare providers
- **Secure Patient Registration**: Comprehensive patient registration with HIPAA compliance
- **Patient Login**: JWT-based authentication for patients with 30-minute token expiry
- **Provider Availability Management**: Complete availability scheduling with recurring patterns
- **Appointment Slot Management**: Automated slot generation and conflict prevention
- **Patient Search & Booking**: Advanced search functionality for available appointments
- **Password Security**: BCrypt hashing with 12 salt rounds
- **Input Sanitization**: Protection against injection attacks
- **Comprehensive Validation**: Email, phone, license number uniqueness
- **Age Validation**: COPPA compliance (minimum 13 years old)
- **RESTful API**: Clean REST endpoints with proper error handling
- **Unit & Integration Tests**: Complete test coverage
- **Database Integration**: JPA/Hibernate with H2 database
- **Swagger Documentation**: Interactive API documentation

## Technology Stack

- **Spring Boot 3.2.0**
- **Spring Security**
- **Spring Data JPA**
- **H2 Database** (for development)
- **JWT (JSON Web Tokens)**
- **Lombok**
- **JUnit 5 & Mockito**
- **Maven**
- **SpringDoc OpenAPI** (Swagger)

## Database Schema

### Provider Entity
```json
{
  "id": "UUID",
  "firstName": "String (2-50 chars, required)",
  "lastName": "String (2-50 chars, required)",
  "email": "String (unique, valid email, required)",
  "phoneNumber": "String (unique, intl format, required)",
  "passwordHash": "String (bcrypt hashed, required)",
  "specialization": "String (3-100 chars, required)",
  "licenseNumber": "String (unique, alphanumeric, required)",
  "yearsOfExperience": "Integer (0-50, optional)",
  "clinicAddress": {
    "street": "String (max 200 chars, required)",
    "city": "String (max 100 chars, required)",
    "state": "String (max 50 chars, required)",
    "zip": "String (valid zip format, required)"
  },
  "verificationStatus": "PENDING | VERIFIED | REJECTED (default: PENDING)",
  "isActive": "Boolean (default: true)",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### Patient Entity
```json
{
  "id": "UUID",
  "firstName": "String (2-50 chars, required)",
  "lastName": "String (2-50 chars, required)",
  "email": "String (unique, valid email, required)",
  "phoneNumber": "String (unique, intl format, required)",
  "passwordHash": "String (bcrypt hashed, required)",
  "dateOfBirth": "Date (past date, age >= 13, required)",
  "gender": "MALE | FEMALE | OTHER | PREFER_NOT_TO_SAY (required)",
  "address": {
    "street": "String (max 200 chars, required)",
    "city": "String (max 100 chars, required)",
    "state": "String (max 50 chars, required)",
    "zip": "String (valid zip format, required)"
  },
  "emergencyContact": {
    "name": "String (max 100 chars, optional)",
    "phone": "String (intl format, optional)",
    "relationship": "String (max 50 chars, optional)"
  },
  "medicalHistory": "Array of Strings (optional)",
  "insuranceInfo": {
    "provider": "String (max 100 chars, optional)",
    "policyNumber": "String (max 50 chars, optional)"
  },
  "emailVerified": "Boolean (default: false)",
  "phoneVerified": "Boolean (default: false)",
  "isActive": "Boolean (default: true)",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### ProviderAvailability Entity
```json
{
  "id": "UUID",
  "providerId": "UUID (foreign key, required)",
  "date": "LocalDate (required)",
  "startTime": "LocalTime (required)",
  "endTime": "LocalTime (required)",
  "timezone": "String (required)",
  "isRecurring": "Boolean (default: false)",
  "recurrencePattern": "DAILY | WEEKLY | MONTHLY (optional)",
  "recurrenceEndDate": "LocalDate (optional)",
  "slotDuration": "Integer (15-480 minutes, default: 30)",
  "breakDuration": "Integer (0-120 minutes, default: 0)",
  "status": "AVAILABLE | BOOKED | CANCELLED | BLOCKED | MAINTENANCE (default: AVAILABLE)",
  "maxAppointmentsPerSlot": "Integer (1-10, default: 1)",
  "currentAppointments": "Integer (default: 0)",
  "appointmentType": "CONSULTATION | FOLLOW_UP | EMERGENCY | TELEMEDICINE (default: CONSULTATION)",
  "location": {
    "type": "CLINIC | HOSPITAL | TELEMEDICINE | HOME_VISIT (required)",
    "address": "String (max 500 chars, required for physical locations)",
    "roomNumber": "String (max 50 chars, optional)"
  },
  "pricing": {
    "baseFee": "BigDecimal (optional)",
    "insuranceAccepted": "Boolean (default: false)",
    "currency": "String (max 3 chars, default: USD)"
  },
  "notes": "String (max 500 chars, optional)",
  "specialRequirements": "Array of Strings (optional)",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

### AppointmentSlot Entity
```json
{
  "id": "UUID",
  "availabilityId": "UUID (foreign key, required)",
  "providerId": "UUID (foreign key, required)",
  "slotStartTime": "LocalDateTime (required)",
  "slotEndTime": "LocalDateTime (required)",
  "status": "AVAILABLE | BOOKED | CANCELLED | BLOCKED (default: AVAILABLE)",
  "patientId": "UUID (foreign key, nullable)",
  "appointmentType": "String",
  "bookingReference": "String (unique, auto-generated)",
  "createdAt": "Timestamp",
  "updatedAt": "Timestamp"
}
```

## API Endpoints

### Provider Management
- **POST** `/api/providers/register` - Register a new provider
- **GET** `/api/providers/{id}` - Get provider by ID
- **GET** `/api/providers/email/{email}` - Get provider by email

### Provider Authentication
- **POST** `/api/v1/provider/login` - Provider login with JWT

### Patient Management
- **POST** `/api/v1/patient/register` - Register a new patient
- **POST** `/api/v1/patient/login` - Patient login with JWT

### Provider Availability Management
- **POST** `/api/v1/provider/availability` - Create availability slots
- **GET** `/api/v1/provider/{providerId}/availability` - Get provider availability
- **PUT** `/api/v1/provider/availability/{slotId}` - Update availability slot
- **DELETE** `/api/v1/provider/availability/{slotId}` - Delete availability slot
- **GET** `/api/v1/provider/availability/search` - Search available slots

## Validation Rules

### Password Requirements
- Minimum 8 characters
- At least one uppercase letter
- At least one lowercase letter
- At least one number
- At least one special character (@$!%*?&)

### Phone Number Format
- International format with + prefix
- Example: +1234567890

### License Number
- Alphanumeric characters only
- No special characters or spaces

### ZIP Code
- 5 digits (12345) or 9 digits with hyphen (12345-6789)

### Availability Validation
- End time must be after start time
- Minimum slot duration: 15 minutes
- Maximum slot duration: 8 hours
- No overlapping slots for same provider
- Future dates only for availability creation

## Security Features

1. **Password Hashing**: BCrypt with 12 salt rounds
2. **JWT Authentication**: Secure token-based authentication
   - Provider tokens: 1-hour expiry
   - Patient tokens: 30-minute expiry
3. **Input Sanitization**: Removes script tags and dangerous characters
4. **Unique Constraints**: Email, phone number, and license number uniqueness
5. **Validation**: Comprehensive field validation with meaningful error messages
6. **Error Handling**: Proper HTTP status codes and error responses
7. **Account Status Check**: Prevents login for deactivated accounts
8. **Case Insensitive Login**: Email addresses are case-insensitive for better UX
9. **Generic Error Messages**: Invalid credentials return the same error message for security
10. **Provider Ownership Validation**: Ensures providers can only modify their own availability
11. **Conflict Prevention**: Prevents overlapping availability slots

## Setup Instructions

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- IntelliJ IDEA (recommended for Java 17 support)

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd think
   ```

2. **Build the project**
   ```bash
   mvn clean install
   ```

3. **Run the application**
   ```bash
   mvn spring-boot:run
   ```

4. **Access the application**
   - Application: http://localhost:8089
   - Swagger UI: http://localhost:8089/swagger-ui.html
   - H2 Console: http://localhost:8089/h2-console
     - JDBC URL: `jdbc:h2:mem:testdb`
     - Username: `sa`
     - Password: `password`

### Running Tests

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=ProviderServiceTest

# Run with coverage
mvn test jacoco:report
```

## Testing

### Unit Tests
- **ProviderServiceTest**: Tests business logic, validation, and password hashing
- **ProviderServiceLoginTest**: Tests authentication logic and JWT token generation
- **ProviderControllerTest**: Tests REST endpoints and validation
- **AuthControllerTest**: Tests login REST endpoints and validation
- **PatientServiceTest**: Tests patient registration logic, validation, and password hashing
- **PatientControllerTest**: Tests patient REST endpoints and validation
- **ProviderAvailabilityServiceTest**: Tests availability management logic
- **ProviderAvailabilityControllerTest**: Tests availability REST endpoints

### Test Coverage
- Service layer validation logic
- Duplicate email/phone scenarios
- Password hashing and verification
- JWT token generation and validation
- Authentication logic and error scenarios
- Input sanitization
- Error handling
- Age validation (COPPA compliance)
- Data privacy and encryption
- HIPAA compliance considerations
- Availability slot creation and conflict detection
- Time zone handling and validation
- Recurring pattern generation
- Search functionality and filtering

## Error Handling

### Validation Errors (400 Bad Request)
```json
{
  "email": "Email must be a valid email address",
  "phoneNumber": "Phone number must be in international format (e.g., +1234567890)",
  "password": "Password must be at least 8 characters long and contain at least one uppercase letter, one lowercase letter, one number, and one special character"
}
```

### Business Logic Errors (400 Bad Request)
```json
{
  "error": "Email already registered: john.doe@example.com"
}
```

**Login Errors:**
```json
{
  "error": "Invalid email or password"
}
```

```json
{
  "error": "Account is deactivated. Please contact support."
}
```

**Availability Errors:**
```json
{
  "error": "Time slot overlaps with existing availability"
}
```

```json
{
  "error": "End time must be after start time"
}
```

### Server Errors (500 Internal Server Error)
```json
{
  "error": "An internal server error occurred"
}
```

## Database Access

### H2 Console
- URL: http://localhost:8089/h2-console
- JDBC URL: `jdbc:h2:mem:testdb`
- Username: `sa`
- Password: `password`

### Sample Queries
```sql
-- View all providers
SELECT * FROM providers;

-- Find provider by email
SELECT * FROM providers WHERE email = 'john.doe@example.com';

-- Find providers by verification status
SELECT * FROM providers WHERE verification_status = 'PENDING';

-- View provider availability
SELECT * FROM provider_availability WHERE provider_id = 'provider-uuid';

-- View appointment slots
SELECT * FROM appointment_slots WHERE provider_id = 'provider-uuid';
```

## Project Structure

```
src/
├── main/
│   ├── java/com/think/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java
│   │   │   └── SwaggerConfig.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProviderController.java
│   │   │   ├── PatientController.java
│   │   │   ├── PatientAuthController.java
│   │   │   └── ProviderAvailabilityController.java
│   │   ├── dto/
│   │   │   ├── ProviderRegistrationRequest.java
│   │   │   ├── ProviderResponse.java
│   │   │   ├── ProviderLoginRequest.java
│   │   │   ├── ProviderLoginResponse.java
│   │   │   ├── PatientRegistrationRequest.java
│   │   │   ├── PatientResponse.java
│   │   │   ├── PatientLoginRequest.java
│   │   │   ├── PatientLoginResponse.java
│   │   │   ├── CreateAvailabilityRequest.java
│   │   │   ├── AvailabilityResponse.java
│   │   │   ├── AvailabilitySearchRequest.java
│   │   │   └── AvailabilitySearchResponse.java
│   │   ├── entity/
│   │   │   ├── Provider.java
│   │   │   ├── ClinicAddress.java
│   │   │   ├── Patient.java
│   │   │   ├── PatientAddress.java
│   │   │   ├── EmergencyContact.java
│   │   │   ├── InsuranceInfo.java
│   │   │   ├── ProviderAvailability.java
│   │   │   ├── AvailabilityLocation.java
│   │   │   ├── AvailabilityPricing.java
│   │   │   └── AppointmentSlot.java
│   │   ├── repository/
│   │   │   ├── ProviderRepository.java
│   │   │   ├── PatientRepository.java
│   │   │   ├── ProviderAvailabilityRepository.java
│   │   │   └── AppointmentSlotRepository.java
│   │   ├── service/
│   │   │   ├── ProviderService.java
│   │   │   ├── PatientService.java
│   │   │   └── ProviderAvailabilityService.java
│   │   ├── util/
│   │   │   └── JwtUtil.java
│   │   └── ThinkApplication.java
│   └── resources/
│       └── application.properties
└── test/
    └── java/com/think/
        ├── controller/
        │   ├── AuthControllerTest.java
        │   ├── ProviderControllerTest.java
        │   ├── PatientControllerTest.java
        │   ├── PatientAuthControllerTest.java
        │   └── ProviderAvailabilityControllerTest.java
        └── service/
            ├── ProviderServiceTest.java
            ├── ProviderServiceLoginTest.java
            ├── PatientServiceTest.java
            ├── PatientServiceLoginTest.java
            └── ProviderAvailabilityServiceTest.java
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the MIT License.
