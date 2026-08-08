package com.prilepskiy_ae.gateway_service.configuration;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

import static org.springframework.web.servlet.function.RequestPredicates.path;


@Configuration
public class GatewayRoutesConfig {

    private final RestTemplate restTemplate;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private static final Logger log = LoggerFactory.getLogger(GatewayRoutesConfig.class);

    @Value("${gateway.services.user-service-url}")
    private String userServiceUrl;

    @Value("${gateway.services.notification-service-url}")
    private String notificationServiceUrl;

    public GatewayRoutesConfig(RestTemplate restTemplate, CircuitBreakerRegistry circuitBreakerRegistry) {
        this.restTemplate = restTemplate;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    @Bean
    public RouterFunction<ServerResponse> userServiceRoute() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("userServiceCircuitBreaker");

        return createRouter(cb,"user_service_route","/api/users/**",userServiceUrl);
    }

    @Bean
    public RouterFunction<ServerResponse> notificationServiceRoute() {
        CircuitBreaker cb = circuitBreakerRegistry.circuitBreaker("notificationServiceCircuitBreaker");

        return createRouter(cb,"notification_service_route","/api/notification/**",notificationServiceUrl);

    }

    private RouterFunction<ServerResponse> createRouter(CircuitBreaker cb,String routeName,String path,String url){
        return GatewayRouterFunctions.route(routeName)
                .route(path(path), request -> {
                    String fullPath = request.uri().getPath();
                    String targetUrl = url + fullPath;

                    try {
                        var response = cb.executeCheckedSupplier(() ->
                                restTemplate.getForEntity(targetUrl, String.class)
                        );

                        int status = response.getStatusCode().value();
                        String body = response.getBody();
                        MediaType contentType = response.getHeaders().getContentType();

                        log.debug("Backend [{}] responded with status={}", targetUrl, status);


                        return ServerResponse.status(status)
                                .contentType(contentType != null ? contentType : MediaType.APPLICATION_JSON)
                                .body(body != null ? body : "{}");

                    }
                    catch (HttpClientErrorException e) {
                        log.error("error for {}: {}", routeName,e.getMessage());
                        int status = e.getStatusCode().value();
                        String body = e.getResponseBodyAsString();

                        return ServerResponse.status(status)
                                .contentType( MediaType.APPLICATION_JSON)
                                .body(body);
                    }
                    catch (Throwable e) {
                        log.error("critical error for {}: {}", routeName, e.getMessage());
                        return ServerResponse.status(503)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body("{\"error\":\"service is unavailable\"}");
                    }
                })
                .build();
    }


}