package com.prilepskiy_ae.userservice.configuration;

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
                Server localServer = new Server()
                        .url("http://localhost:8081")
                        .description("Local server");

                Contact contact = new Contact()
                        .name("Alex")
                        .email("alex@aston.ru")
                        .url("https://example.com");

                License license = new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT");

                Info info = new Info()
                        .title("User Management API")
                        .version("1.0.0")
                        .description("API для управления пользователями")
                        .contact(contact)
                        .license(license);

                return new OpenAPI()
                        .info(info)
                        .servers(List.of(localServer));
        }
}
