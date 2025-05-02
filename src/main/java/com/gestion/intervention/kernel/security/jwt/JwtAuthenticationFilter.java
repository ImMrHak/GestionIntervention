package com.gestion.intervention.kernel.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gestion.intervention.kernel.security.jwt.userPrincipal.UserPrincipal; // Assuming this just holds userId and email
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger; // Add Logger
import org.slf4j.LoggerFactory; // Add Logger Factory
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;
import java.time.Instant;
import java.util.Collection; // Import Collection
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.springframework.security.core.GrantedAuthority; // Import GrantedAuthority

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class); // Add logger

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final JwtService jwtService;
    private final ObjectMapper objectMapper = new ObjectMapper(); // Keep using ObjectMapper for error responses

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // No token, continue chain
            return;
        }

        String jwt = null;
        String userEmail = null;
        UUID userId = null;
        String tokenType = null;

        try {
            jwt = authHeader.substring(7);
            userEmail = jwtService.extractUsername(jwt); // Extract email (subject)
            userId = jwtService.extractUserId(jwt);       // Extract userId claim
            tokenType = jwtService.extractClaim(jwt, claims -> claims.get("type", String.class)); // Check if it's a refresh token

            // Proceed only if we have user identifier and no existing authentication
            if (userId != null && userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                boolean isRefreshToken = "refresh".equals(tokenType);

                // Block refresh token usage for general resources
                if (isRefreshToken && !request.getServletPath().contains("/auth/refresh")) { // Be more robust checking refresh path if needed
                    log.warn("Attempt to use refresh token for resource access: {}", request.getServletPath());
                    sendJsonResponse(response, HttpServletResponse.SC_FORBIDDEN, "Invalid Token Usage", "Refresh token cannot be used for this resource", "refresh", null, userEmail);
                    return;
                }

                // Validate ACCESS token (ignore validation for refresh token path itself)
                // Refresh token validation happens in the refresh endpoint logic
                if (!isRefreshToken) {
                    if (jwtService.isTokenValid(jwt, userEmail)) {
                        // Token is valid, extract authorities (roles)
                        Collection<? extends GrantedAuthority> authorities = jwtService.extractAuthorities(jwt);

                        // Create UserPrincipal (adapt if it needs roles)
                        UserPrincipal userPrincipal = new UserPrincipal(userId, userEmail);
                        // Or if UserPrincipal needs authorities:
                        // UserPrincipal userPrincipal = new UserPrincipal(userId, userEmail, authorities);

                        // Create authentication token
                        UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                                userPrincipal, // Principal can be UserPrincipal or just email/userId
                                null,          // Credentials (not needed for JWT auth)
                                authorities    // Authorities extracted from token
                        );

                        // Set details
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                        // Set authentication in SecurityContext
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        log.debug("Successfully authenticated user '{}' with roles {}", userEmail, authorities);

                    } else {
                        log.warn("Invalid or expired access token provided for user {}", userEmail);
                        // Optionally send error response here, but often letting the chain proceed
                        // results in a 401/403 from subsequent security checks if needed.
                        // For clarity, we can send it now:
                        sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token", "Invalid or expired access token", "access", null, userEmail);
                        return; // Stop the filter chain
                    }
                }
                // If it's a refresh token, we don't set authentication here.
                // The refresh endpoint controller will handle its validation.
                // We just let the filter chain proceed.
            }

            filterChain.doFilter(request, response); // Continue chain

        } catch (Exception exception) {
            // Catch potential errors during JWT parsing or processing
            log.error("JWT Filter Error: {}", exception.getMessage(), exception);
            // Use HandlerExceptionResolver to handle the exception globally, or send a generic error
            // Sending a generic error response directly:
            sendJsonResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Authentication Error", "Error processing authentication token: " + exception.getMessage(), tokenType, null, userEmail);
            // Optionally delegate to handlerExceptionResolver:
            // handlerExceptionResolver.resolveException(request, response, null, exception);
        }
    }

    // sendJsonResponse method remains the same
    private void sendJsonResponse(HttpServletResponse response, int status, String error, String message, String tokenType, Instant expirationTime, String userEmail) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("status", status);
        responseBody.put("error", error);
        responseBody.put("message", message);
        // Optionally include tokenType, expiresAt, userEmail for debugging, but be cautious with sensitive info
        // responseBody.put("tokenType", tokenType);
        // responseBody.put("expiresAt", expirationTime != null ? expirationTime.toString() : null);
        // responseBody.put("userEmail", userEmail); // Consider removing email from error response
        responseBody.put("timestamp", Instant.now().toString());
        response.getWriter().write(objectMapper.writeValueAsString(responseBody));
    }
}