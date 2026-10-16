package com.practice.product_service.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final ObjectMapper mapper;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) {

        AuthenticationEntryPoint unauthorized = (request, response, exception) -> {
            log.debug("Authentication failed for {}: {}", request.getRequestURI(),
                    exception.getMessage());

            response.addHeader("WWW-Authenticate", "Bearer");
            writeResponse(request, response, HttpStatus.UNAUTHORIZED, "Authentication required");
        };

        AccessDeniedHandler accessDenied = (request, response, exception) -> {
            writeResponse(request, response, HttpStatus.FORBIDDEN, "Access denied");
        };

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRoleConverter()))
                        .authenticationEntryPoint(unauthorized))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(unauthorized)
                        .accessDeniedHandler(accessDenied))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/products/**").hasAnyRole("USER",
                                "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/products/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/products/**").hasRole("ADMIN")
                        .anyRequest().denyAll());
        return http.build();
    }

    private JwtAuthenticationConverter keycloakRoleConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(new KeycloakRealmRoleConverter());
        return converter;
    }

    private void writeResponse(HttpServletRequest request, HttpServletResponse response,
                               HttpStatus status,
                               String detail) throws IOException {
        response.setContentType("application/problem+json");
        response.setCharacterEncoding("UTF-8");
        response.setStatus(status.value());

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.getRequestURI()));
        response.getWriter().write(mapper.writeValueAsString(problem));
    }

}