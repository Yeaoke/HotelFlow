package com.example.app.config;
import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.*;
@Configuration
public class OpenApiConfig {
    @Bean public OpenAPI hotelFlowApi() {
        return new OpenAPI().info(new Info().title("HotelFlow API").version("1.0")
            .description("Email verification, rotating sessions, rooms, reservations and private stay reviews. Dates use [start, end); prices are integer minor currency units."))
            .components(new Components().addSecuritySchemes("Bearer Authentication",
                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));
    }
}
