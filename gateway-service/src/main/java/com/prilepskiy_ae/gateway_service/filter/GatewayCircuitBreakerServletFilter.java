package com.prilepskiy_ae.gateway_service.filter;

import com.prilepskiy_ae.gateway_service.controller.GatewayFallbackController;
import com.prilepskiy_ae.gateway_service.utils.Utils;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Component
public class GatewayCircuitBreakerServletFilter extends OncePerRequestFilter {

    private final CircuitBreaker userServiceCircuitBreaker;
    private final CircuitBreaker notificationServiceCircuitBreaker;
    private final GatewayFallbackController fallbackController;

    public GatewayCircuitBreakerServletFilter(
            CircuitBreakerRegistry circuitBreakerRegistry,
            GatewayFallbackController fallbackController
    ) {
        this.userServiceCircuitBreaker =
                circuitBreakerRegistry.circuitBreaker("userServiceCircuitBreaker");
        this.notificationServiceCircuitBreaker =
                circuitBreakerRegistry.circuitBreaker("notificationServiceCircuitBreaker");
        this.fallbackController = fallbackController;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String path = request.getRequestURI();

        CircuitBreaker circuitBreaker = resolveCircuitBreaker(path);

        if (circuitBreaker == null) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.nanoTime();

        try {
            circuitBreaker.acquirePermission();

            filterChain.doFilter(request, response);

            long duration = System.nanoTime() - start;


            if (shouldRecordAsFailure(path, response.getStatus())) {
                circuitBreaker.onError(
                        duration,
                        TimeUnit.NANOSECONDS,
                        new RuntimeException("HTTP status " + response.getStatus())
                );
            } else {
                circuitBreaker.onSuccess(duration, TimeUnit.NANOSECONDS);
            }

        } catch (CallNotPermittedException e) {
            writeFallback(path, response);
        }
    }

    private CircuitBreaker resolveCircuitBreaker(String path) {
        if (Utils.isUserPath(path)) {
            return userServiceCircuitBreaker;
        }

        if (Utils.isNotificationPath(path)) {
            return notificationServiceCircuitBreaker;
        }

        return null;
    }

    private void writeFallback(
            String path,
            HttpServletResponse response
    ) throws IOException {
        ResponseEntity<String> fallback;

        if (Utils.isNotificationPath(path)) {
            fallback = fallbackController.notificationServiceUnavailable();
        } else {
            fallback = fallbackController.userServiceUnavailable();
        }

        response.setStatus(fallback.getStatusCode().value());
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(fallback.getBody());
    }

    private boolean shouldRecordAsFailure(String path, int status) {
        if (status >= 500) {
            return true;
        }
        // из требований не понятно ловит нужно все 4xx, по этому ловим все
        if ((Utils.isUserPath(path) || Utils.isNotificationPath(path)) && status >= 400 && status < 500) {
            return true;
        }

        return false;
    }

}