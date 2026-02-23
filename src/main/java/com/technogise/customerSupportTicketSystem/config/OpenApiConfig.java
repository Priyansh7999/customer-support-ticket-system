package com.technogise.customerSupportTicketSystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Customer Support Ticket System API")
                        .version("1.0.0")
                        .description("Documentation for the Customer Ticket Support System: " +
                                "Use the 'User-Id' header for authorized requests."));
    }
}