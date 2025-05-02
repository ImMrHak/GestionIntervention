package com.gestion.intervention.kernel.security;

import com.gestion.intervention.kernel.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                /*.exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint((request, response, authException) -> {
                            // Set content type as JSON
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                            // Create an improved error message with more context
                            String jsonResponse = "{"
                                    + "\"status\": \"error\","
                                    + "\"error\": \"Unauthorized\","
                                    + "\"message\": \"Invalid credentials or session expired.\","
                                    + "\"code\": 401,"
                                    + "\"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\""
                                    + "}";

                            // Write the response
                            response.getWriter().write(jsonResponse);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            // Set content type as JSON
                            response.setContentType("application/json");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);

                            // Create an improved error message with more context
                            String jsonResponse = "{"
                                    + "\"status\": \"error\","
                                    + "\"error\": \"Forbidden\","
                                    + "\"message\": \"You do not have permission to access this resource.\","
                                    + "\"code\": 403,"
                                    + "\"timestamp\": \"" + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME) + "\""
                                    + "}";

                            // Write the response
                            response.getWriter().write(jsonResponse);
                        })
                )*/
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/hello/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/user/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/v1/user/helper/**").hasAnyRole("HELPDESK", "ADMIN")
                        .requestMatchers("/api/v1/user/technician/**").hasAnyRole("TECHNICIAN", "HELPDESK", "ADMIN")
                        .requestMatchers("/api/v1/user/employee/**").hasAnyRole("EMPLOYEE", "TECHNICIAN", "HELPDESK", "ADMIN")
                        .anyRequest().denyAll())
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:4200"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        configuration.setAllowCredentials(true);
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setExposedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
