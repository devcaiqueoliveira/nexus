package com.devcaiqueoliveira.nexus_api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Nexus API")
                        .version("1.0.0")
                        .description("""
                                API REST para gerenciamento de estudos.
                                """)
                        .contact(new Contact()
                                .name("Caique Oliveira")
                                .email("devcaiqueoliveira@gmail.com")
                                .url("https://github.com/devcaiqueoliveira")));
    }
}
