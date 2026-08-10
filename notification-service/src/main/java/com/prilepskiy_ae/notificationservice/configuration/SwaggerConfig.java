package com.prilepskiy_ae.notificationservice.configuration;

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
                        .url("http://localhost:8082")
                        .description("Local server");

                Contact contact = new Contact()
                        .name("Alex")
                        .email("alex@aston.ru")
                        .url("https://example.com");

                License license = new License()
                        .name("MIT License")
                        .url("https://opensource.org/licenses/MIT");

                Info info = new Info()
                        .title("User Notification Management API")
                        .version("1.0.0")
                        .description("API для отправки увидомления")
                        .contact(contact)
                        .license(license);

                return new OpenAPI()
                        .info(info)
                        .servers(List.of(localServer));
        }
}
