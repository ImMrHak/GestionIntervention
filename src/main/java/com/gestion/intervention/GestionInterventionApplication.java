package com.gestion.intervention;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
@OpenAPIDefinition(
        info = @Info(
                title = "Gestion Intervention API",
                version = "1.0.0",
                description = "API documentation for the Intervention Management System. Provides endpoints for Admins, Employees, and Technicians.",
                contact = @Contact(
                        name = "Support Team",
                        email = "support@example.com",
                        url = "https://example.com/support"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        )
        // You can also define servers, security schemes here if needed
        // servers = @Server(...)
        // security = @SecurityRequirement(...)
)
public class GestionInterventionApplication {

    public static void main(String[] args) {
        SpringApplication.run(GestionInterventionApplication.class, args);
    }

}
