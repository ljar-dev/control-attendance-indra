package com.indra.attendance_control.config;

import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;



@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "User API",
        version = "v1",
        description = "API for user management"
    ),
    security =  @SecurityRequirement(name = "bearerAuth")
)


@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
    // This class is used to configure OpenAPI documentation for the User API
}
