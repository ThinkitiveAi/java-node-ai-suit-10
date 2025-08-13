# Healthcare Application - Project Documentation

## Project Overview

**Healthcare Provider Management System** is a Spring Boot-based REST API application that manages healthcare providers, patients, appointments, and availability scheduling. The system provides comprehensive functionality for healthcare appointment booking with JWT-based authentication and role-based access control.

## Technology Stack

### Backend Framework
- **Spring Boot 3.2.0** - Main application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **Spring Web** - REST API endpoints

### Database
- **H2 Database** - File-based persistent storage
- **Hibernate** - ORM framework
- **JPA** - Data persistence API

### Security
- **JWT (JSON Web Tokens)** - Stateless authentication
- **BCrypt** - Password encryption (strength 12)
- **Role-based Access Control** - Provider/Patient permissions

### Additional Libraries
- **Lombok** - Reduce boilerplate code
- **Jackson** - JSON serialization/deserialization
- **Validation API** - Input validation
- **Swagger/OpenAPI 3** - API documentation

## Project Structure

```
src/
├── main/
│   ├── java/com/think/
│   │   ├── config/           # Configuration classes
│   │   │   ├── SecurityConfig.java
│   │   │   ├── SwaggerConfig.java
│   │   │   └── JwtAuthenticationFilter.java
│   │   ├── controller/       # REST controllers
│   │   │   ├── AuthController.java
│   │   │   ├── PatientAuthController.java
│   │   │   ├── PatientController.java
│   │   │   ├── ProviderController.java
│   │   │   ├── ProviderAvailabilityController.java
│   │   │   └── AppointmentController.java
│   │   ├── dto/              # Data Transfer Objects
│   │   │   ├── *Request.java
│   │   │   └── *Response.java
│   │   ├── entity/           # JPA entities
│   │   │   ├── Provider.java
│   │   │   ├── Patient.java
│   │   │   ├── ProviderAvailability.java
│   │   │   ├── AppointmentSlot.java
│   │   │   └── *Address.java
│   │   ├── repository/       # Data repositories
│   │   │   ├── ProviderRepository.java
│   │   │   ├── PatientRepository.java
│   │   │   ├── ProviderAvailabilityRepository.java
│   │   │   └── AppointmentSlotRepository.java
│   │   ├── service/          # Business logic
│   │   │   ├── ProviderService.java
│   │   │   ├── PatientService.java
│   │   │   ├── ProviderAvailabilityService.java
│   │   │   └── AppointmentService.java
│   │   ├── util/             # Utility classes
│   │   │   └── JwtUtil.java
│   │   └── ThinkApplication.java
│   └── resources/
│       └── application.properties
└── test/                     # Unit tests
```

## Core Features

### 1. User Management
- **Provider Registration/Login** - Healthcare providers can register and authenticate
- **Patient Registration/Login** - Patients can create accounts and authenticate
- **JWT Authentication** - Secure token-based authentication
- **Role-based Access** - Different permissions for providers and patients

### 2. Availability Management
- **Create Availability** - Providers can set their available time slots
- **Recurring Schedules** - Support for daily, weekly, monthly patterns
- **Slot Management** - 30-minute slots with configurable breaks
- **Location Support** - Clinic, hospital, telemedicine, home visit options

### 3. Appointment Booking
- **Search Available Slots** - Find providers by specialization, location, date
- **Book Appointments** - Patients can book available time slots
- **Appointment Management** - View, update, cancel appointments
- **Booking References** - Unique reference codes for each appointment

### 4. Data Management
- **Persistent Storage** - File-based H2 database with data persistence
- **Comprehensive Validation** - Input validation on all endpoints
- **Error Handling** - Detailed error messages and proper HTTP status codes

## Database Schema

### Core Entities

#### Provider
- ID, name, email, phone, specialization
- License number, years of experience
- Clinic address, verification status
- Created/updated timestamps

#### Patient
- ID, name, email, phone, date of birth, gender
- Address, emergency contact, medical history
- Insurance information, verification status
- Created/updated timestamps

#### ProviderAvailability
- ID, provider reference, date, time range
- Slot duration, break duration, timezone
- Appointment type, location, pricing
- Recurrence pattern, special requirements

#### AppointmentSlot
- ID, availability reference, provider reference
- Start/end time, status, booking reference
- Patient reference (when booked)
- Created/updated timestamps

## Security Implementation

### Authentication Flow
1. User registers (provider/patient)
2. User logs in with credentials
3. System validates credentials and generates JWT token
4. Token contains user role and relevant IDs
5. Token must be included in Authorization header for protected endpoints
6. System validates token and extracts user context for each request

### JWT Token Structure
```json
{
  "role": "PROVIDER|PATIENT",
  "provider_id": "uuid",
  "patient_id": "uuid",
  "email": "user@example.com",
  "specialization": "specialty",
  "iat": 1755105315,
  "exp": 1755108915
}
```

### Endpoint Security
- **Public**: Registration, login, availability search
- **Provider Only**: Availability management, provider details
- **Patient/Provider**: Appointment booking and management
- **Protected**: All other endpoints require authentication

## API Endpoints Summary

### Public Endpoints (No Authentication)
- `POST /api/providers/register` - Provider registration
- `POST /api/v1/provider/login` - Provider login
- `POST /api/v1/patient/register` - Patient registration
- `POST /api/v1/patient/login` - Patient login
- `GET /api/v1/provider/availability/search` - Search available slots

### Provider-Only Endpoints
- `POST /api/v1/provider/availability` - Create availability
- `GET /api/v1/provider/{id}/availability` - Get provider availability
- `PUT /api/v1/provider/availability/{slotId}` - Update availability
- `DELETE /api/v1/provider/availability/{slotId}` - Delete availability
- `GET /api/providers/**` - Provider management

### Patient/Provider Endpoints
- `POST /api/appointments/book` - Book appointment
- `GET /api/appointments` - List appointments
- `GET /api/appointments/{reference}` - Get appointment details
- `PUT /api/appointments/{reference}/cancel` - Cancel appointment

## Configuration

### Application Properties
```properties
# Server Configuration
server.port=8088

# Database Configuration
spring.datasource.url=jdbc:h2:file:./data/healthcare_db
spring.datasource.username=sa
spring.datasource.password=password
spring.jpa.hibernate.ddl-auto=update

# JWT Configuration
jwt.secret=healthcareProviderSystemSecretKey2024ForSecureAuthentication
jwt.expiration=3600

# H2 Console (Development)
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Security Configuration
- CORS enabled for cross-origin requests
- CSRF disabled for REST API
- Stateless session management
- JWT filter for token validation
- Role-based endpoint protection

## Development Setup

### Prerequisites
- Java 17+
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Running the Application
```bash
# Clone repository
git clone <repository-url>

# Navigate to project directory
cd think

# Run with Maven
./mvnw spring-boot:run

# Or build and run JAR
./mvnw clean package
java -jar target/think-0.0.1-SNAPSHOT.jar
```

### Database Access
- **H2 Console**: http://localhost:8088/h2-console
- **JDBC URL**: jdbc:h2:file:./data/healthcare_db
- **Username**: sa
- **Password**: password

### API Documentation
- **Swagger UI**: http://localhost:8088/swagger-ui.html
- **OpenAPI Spec**: http://localhost:8088/v3/api-docs

## Testing

### Unit Tests
- Controller tests with MockMvc
- Service layer tests with Mockito
- Repository tests with @DataJpaTest
- Integration tests for complete workflows

### Manual Testing
- Postman collection available
- Curl commands documented
- Test data scripts provided

## Deployment

### Docker Support
```dockerfile
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/think-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8088
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Build and Run
```bash
# Build application
./mvnw clean package

# Build Docker image
docker build -t healthcare-app .

# Run container
docker run -p 8088:8088 healthcare-app
```

## Performance Considerations

### Database Optimization
- Proper indexing on frequently queried fields
- Lazy loading for entity relationships
- Pagination for large result sets
- Connection pooling for database connections

### Caching Strategy
- JWT token validation caching
- Provider availability caching
- Static data caching (specializations, locations)

### Scalability
- Stateless architecture with JWT
- Horizontal scaling support
- Load balancer compatibility
- Database connection pooling

## Monitoring and Logging

### Application Logging
- Structured logging with Logback
- Different log levels for different packages
- Request/response logging for debugging
- Error tracking and alerting

### Health Checks
- Spring Boot Actuator endpoints
- Database connectivity checks
- JWT service health monitoring
- Custom health indicators

## Future Enhancements

### Planned Features
- Email notifications for appointments
- SMS reminders for patients
- Payment integration
- Telemedicine video calls
- Provider ratings and reviews
- Advanced search filters
- Mobile app support

### Technical Improvements
- Redis caching layer
- PostgreSQL for production
- Microservices architecture
- Event-driven architecture
- API rate limiting
- Advanced monitoring with Prometheus/Grafana

## Support and Maintenance

### Documentation
- API documentation with Swagger
- Code documentation with JavaDoc
- Database schema documentation
- Deployment guides

### Version Control
- Git-based version control
- Feature branch workflow
- Code review process
- Automated testing pipeline

### Issue Tracking
- Bug reporting system
- Feature request tracking
- Performance monitoring
- Security vulnerability scanning