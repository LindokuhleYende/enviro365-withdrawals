package com.enviro.assessment.junior.lindokuhleyende.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI enviro365OpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Enviro365 Investments - Withdrawal Notice API")
                        .description("REST API for viewing investor portfolios, submitting withdrawal notices, "
                                + "and exporting CSV withdrawal statements.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Lindokuhle Yende")
                                .email("dev@enviro365.example.com")));
    }
}
