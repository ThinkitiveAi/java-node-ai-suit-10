package com.think.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SwaggerConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Healthcare Provider & Patient Management API")
                .description("""
                    A comprehensive REST API for healthcare provider and patient management system.
                    
                    ## Features
                    - **Provider Registration & Authentication**: Secure registration and JWT-based login for healthcare providers
                    - **Patient Registration & Authentication**: Comprehensive patient registration with HIPAA compliance
                    - **Provider Availability Management**: Complete availability scheduling with recurring patterns
                    - **Appointment Slot Management**: Automated slot generation and conflict prevention
                    - **Patient Search & Booking**: Advanced search functionality for available appointments
                    
                    ## Authentication
                    - JWT-based authentication for both providers and patients
                    - Provider tokens: 1-hour expiry
                    - Patient tokens: 30-minute expiry
                    
                    ## Security Features
                    - BCrypt password hashing (12 salt rounds)
                    - Input sanitization and validation
                    - HIPAA compliance considerations
                    - Timezone-aware scheduling
                    
                    ## API Endpoints
                    - Provider Management: `/api/providers/*`
                    - Provider Authentication: `/api/v1/provider/login`
                    - Patient Management: `/api/v1/patient/*`
                    - Availability Management: `/api/v1/provider/availability/*`
                    - Appointment Search: `/api/v1/provider/availability/search`
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Healthcare API Team")
                    .email("api@healthcare.com")
                    .url("https://healthcare-api.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8089")
                    .description("Development Server"),
                new Server()
                    .url("https://api.healthcare.com")
                    .description("Production Server")
            ));
    }
}
