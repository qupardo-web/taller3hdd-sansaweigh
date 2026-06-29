package com.sansaweigh.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI sansaweighOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("SansaWeigh API")
                        .description("Microservicio de gestión de estaciones de pesaje de paquetes")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SansaWeigh Team")));
    }
}
