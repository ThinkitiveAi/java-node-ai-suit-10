# Healthcare Application - Provider & Patient Management System

A comprehensive Spring Boot REST API application for healthcare provider and patient management, appointment booking, and availability scheduling with JWT-based authentication and role-based access control.

## Features

### Core Functionality
- **Provider Management**: Registration, authentication, and profile management
- **Patient Management**: Registration, authentication, and profile management
- **Availability Management**: Create, update, delete provider availability slots
- **Appointment Booking**: Search, book, view, and cancel appointments
- **Search & Discovery**: Find providers by specialization, location, and availability

### Security & Authentication
- **JWT Authentication**: Stateless token-based authentication
- **Role-based Access Control**: Provider and Patient roles with different permissions
- **Password Security**: BCrypt hashing with 12 salt rounds
- **Input Validation**: Comprehensive validation with sanitization
- **Token Expiration**: Provider (1 hour), Patient (30 minutes)

### Technical Features
- **RESTful API**: Clean REST endpoints with proper HTTP status codes
- **Database Persistence**: File-based H2 database with data persistence
- **Automated Slot Generation**: 30-minute slots with configurable breaks
- **Conflict Prevention**: Prevents overlapping availability slots
- **Comprehensive Testing**: Unit and integration tests with high coverage
- **API Documentation**: Interactive Swagger/OpenAPI documentation
- **Docker Support**: Containerized deployment ready

## Technology Stack

### Backend Framework
- **Spring Boot 3.2.0** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **Spring Web** - REST API endpoints
- **Spring Validation** - Input validation

### Database & Persistence
- **H2 Database** - File-based persistent storage
- **Hibernate** - ORM framework
- **JPA** - Data persistence API

### Security & Authentication
- **JWT (JJWT 0.12.3)** - JSON Web Tokens
- **BCrypt** - Password encryption

### Development & Testing
- **Java 17** - Programming language
- **Maven** - Build tool
- **Lombok** - Code generation
- **JUnit 5** - Testing framework
- **Mockito** - Mocking framework
- **SpringDoc OpenAPI 2.2.0** - API documentation

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

### Public Endpoints (No Authentication Required)
- **POST** `/api/providers/register` - Register a new provider
- **POST** `/api/v1/provider/login` - Provider login with JWT
- **POST** `/api/v1/patient/register` - Register a new patient
- **POST** `/api/v1/patient/login` - Patient login with JWT
- **GET** `/api/v1/provider/availability/search` - Search available slots

### Provider-Only Endpoints (Requires Provider JWT)
- **POST** `/api/v1/provider/availability` - Create availability slots
- **GET** `/api/v1/provider/{providerId}/availability` - Get provider availability
- **PUT** `/api/v1/provider/availability/{slotId}` - Update availability slot
- **DELETE** `/api/v1/provider/availability/{slotId}` - Delete availability slot
- **GET** `/api/providers` - Get all providers
- **GET** `/api/providers/{id}` - Get provider by ID
- **GET** `/api/providers/email/{email}` - Get provider by email

### Patient & Provider Endpoints (Requires JWT)
- **POST** `/api/appointments/book` - Book an appointment
- **GET** `/api/appointments` - Get appointments list
- **GET** `/api/appointments/{bookingReference}` - Get appointment by reference
- **PUT** `/api/appointments/{bookingReference}/cancel` - Cancel appointment

### Development Endpoints
- **GET** `/h2-console` - H2 database console
- **GET** `/swagger-ui.html` - API documentation
- **GET** `/v3/api-docs` - OpenAPI specification

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

### Quick Start

```bash
# Clone and run
git clone <repository-url>
cd think
./mvnw spring-boot:run

# Application will be available at http://localhost:8088
```

### Detailed Setup

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd think
   ```

2. **Build the project**
   ```bash
   ./mvnw clean install
   ```

3. **Run the application**
   ```bash
   ./mvnw spring-boot:run
   ```

4. **Access the application**
   - Application: http://localhost:8088
   - Swagger UI: http://localhost:8088/swagger-ui.html
   - H2 Console: http://localhost:8088/h2-console
     - JDBC URL: `jdbc:h2:file:./data/healthcare_db`
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
- URL: http://localhost:8088/h2-console
- JDBC URL: `jdbc:h2:file:./data/healthcare_db`
- Username: `sa`
- Password: `password`
- Database Type: File-based (persistent storage)

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
│   │   │   ├── SwaggerConfig.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── controller/
│   │   │   ├── AuthController.java
│   │   │   ├── ProviderController.java
│   │   │   ├── PatientController.java
│   │   │   ├── PatientAuthController.java
│   │   │   ├── ProviderAvailabilityController.java
│   │   │   └── AppointmentController.java
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
│   │   │   ├── AvailabilitySearchResponse.java
│   │   │   ├── BookAppointmentRequest.java
│   │   │   ├── AppointmentResponse.java
│   │   │   └── AppointmentListResponse.java
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
│   │   │   ├── ProviderAvailabilityService.java
│   │   │   └── AppointmentService.java
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
        │   ├── ProviderAvailabilityControllerTest.java
        │   └── AppointmentControllerTest.java
        └── service/
            ├── ProviderServiceTest.java
            ├── ProviderServiceLoginTest.java
            ├── PatientServiceTest.java
            ├── PatientServiceLoginTest.java
            ├── ProviderAvailabilityServiceTest.java
            └── AppointmentServiceTest.java
```

## Docker Support

### Build and Run with Docker

1. **Build the application**
   ```bash
   ./mvnw clean package
   ```

2. **Build Docker image**
   ```bash
   docker build -t healthcare-app .
   ```

3. **Run container**
   ```bash
   docker run -p 8088:8088 healthcare-app
   ```

## Authentication Examples

### Provider Authentication Flow

1. **Register Provider**
   ```bash
   curl -X POST http://localhost:8088/api/providers/register \
     -H "Content-Type: application/json" \
     -d '{
       "firstName": "Dr. Sarah",
       "lastName": "Johnson",
       "email": "sarah.johnson@healthcare.com",
       "password": "Password123!",
       "phoneNumber": "+1555999888",
       "specialization": "Dermatology",
       "licenseNumber": "MD789012",
       "yearsOfExperience": 8,
       "clinicAddress": {
         "street": "456 Health Ave",
         "city": "Boston",
         "state": "MA",
         "zip": "02101"
       }
     }'
   ```

2. **Login Provider**
   ```bash
   curl -X POST http://localhost:8088/api/v1/provider/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "sarah.johnson@healthcare.com",
       "password": "Password123!"
     }'
   ```

3. **Use JWT Token for Protected Endpoints**
   ```bash
   curl -X POST "http://localhost:8088/api/v1/provider/availability?providerId=<provider-id>" \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <jwt-token>" \
     -d '{
       "date": "2025-12-25",
       "startTime": "09:00",
       "endTime": "17:00",
       "timezone": "America/New_York",
       "location": {
         "type": "CLINIC",
         "address": "456 Health Ave, Boston, MA 02101"
       }
     }'
   ```

### Patient Authentication Flow

1. **Register Patient**
   ```bash
   curl -X POST http://localhost:8088/api/v1/patient/register \
     -H "Content-Type: application/json" \
     -d '{
       "first_name": "Alice",
       "last_name": "Smith",
       "email": "alice.smith@email.com",
       "password": "Password123!",
       "confirm_password": "Password123!",
       "phone_number": "+1555777999",
       "date_of_birth": "1985-03-20",
       "gender": "FEMALE",
       "address": {
         "street": "789 Main St",
         "city": "Boston",
         "state": "MA",
         "zip": "02102"
       },
       "emergency_contact": {
         "name": "Bob Smith",
         "phone": "+1555888777",
         "relationship": "Spouse"
       },
       "medical_history": ["Allergies"],
       "insurance_info": {
         "provider": "HealthCare Plus",
         "policy_number": "HP123456"
       }
     }'
   ```

2. **Login Patient**
   ```bash
   curl -X POST http://localhost:8088/api/v1/patient/login \
     -H "Content-Type: application/json" \
     -d '{
       "email": "alice.smith@email.com",
       "password": "Password123!"
     }'
   ```

3. **Book Appointment**
   ```bash
   curl -X POST http://localhost:8088/api/appointments/book \
     -H "Content-Type: application/json" \
     -H "Authorization: Bearer <patient-jwt-token>" \
     -d '{
       "patientId": "<patient-id>",
       "providerId": "<provider-id>",
       "appointmentDate": "2025-12-25",
       "appointmentTime": "10:00",
       "appointmentType": "CONSULTATION",
       "appointmentMode": "IN_PERSON",
       "reasonForVisit": "Skin consultation and checkup"
     }'
   ```

## Test Data

### Sample Provider
- **Email**: `sarah.johnson@healthcare.com`
- **Password**: `Password123!`
- **Specialization**: Dermatology
- **Location**: Boston, MA

### Sample Patient
- **Email**: `alice.smith@email.com`
- **Password**: `Password123!`
- **Location**: Boston, MA

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Run the test suite
6. Submit a pull request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Support

For support and questions:
- Check the [API Documentation](API_DOCUMENTATION.md)
- Review the [Project Documentation](PROJECT_DOCUMENTATION.md)
- Open an issue on GitHub

---

**Healthcare Application** - A comprehensive healthcare provider and patient management system with JWT authentication and role-based access control.
