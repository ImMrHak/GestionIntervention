package com.gestion.intervention.kernel.security.jwt;

import com.gestion.intervention.domain.person.model.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger; // Add Logger
import org.slf4j.LoggerFactory; // Add Logger Factory
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors; // Add Collectors import

@Service
public class JwtService {

    private static final Logger log = LoggerFactory.getLogger(JwtService.class); // Add logger

    @Value("${security.jwt.secret-key}")
    private String secretKey;

    @Value("${security.jwt.access-token-expiration-time}")
    private long jwtAccessExpiration;

    @Value("${security.jwt.refresh-token-expiration-time}")
    private long jwtRefreshExpiration;

    // --- Claim Extraction Methods (mostly unchanged) ---

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public UUID extractUserId(String token) {
        try {
            String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
            return userIdStr != null ? UUID.fromString(userIdStr) : null;
        } catch (Exception e) {
            log.error("Error extracting userId from token", e);
            return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        if (claims == null) return null; // Handle parsing errors
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts
                    .parserBuilder()
                    .setSigningKey(getSignInKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            log.warn("Could not parse JWT claims: {}", e.getMessage()); // Log parsing errors
            return null;
        }
    }

    // --- Token Generation Methods (MODIFIED) ---

    /**
     * Generates an access token for the given user.
     * Includes subject (email), userId, and roles as claims.
     */
    public String generateAccessToken(Person userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();

        // **MODIFIED: Add roles as a list of strings**
        List<String> roles = userDetails.getAuthorities().stream() // Assuming Person implements UserDetails or has getAuthorities()
                .map(GrantedAuthority::getAuthority) // Get the string representation (e.g., "ROLE_EMPLOYEE")
                .collect(Collectors.toList());
        extraClaims.put("roles", roles); // Use a dedicated "roles" claim

        // Add userId claim
        extraClaims.put("userId", userDetails.getId().toString());

        log.debug("Generating access token for user {} with roles: {}", userDetails.getEmail(), roles);
        return buildToken(extraClaims, userDetails, jwtAccessExpiration);
    }

    /**
     * Generates a refresh token for the given user.
     * Includes subject (email), userId, and a "type" claim. Does NOT include roles.
     */
    public String generateRefreshToken(Person userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userDetails.getId().toString());
        extraClaims.put("type", "refresh"); // Mark as refresh token

        log.debug("Generating refresh token for user {}", userDetails.getEmail());
        return buildToken(extraClaims, userDetails, jwtRefreshExpiration);
    }

    // buildToken remains the same logic
    private String buildToken(Map<String, Object> extraClaims, Person userDetails, long expiration) {
        return Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername()) // Should be the unique identifier, often email
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // --- Token Validation Methods (mostly unchanged) ---

    public boolean isTokenValid(String token, String username) {
        try {
            final String tokenUsername = extractUsername(token);
            return (username.equals(tokenUsername)) && !isTokenExpired(token);
        } catch (Exception e) {
            log.warn("Token validation failed: {}", e.getMessage());
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        Date expiration = extractExpiration(token);
        return expiration != null && expiration.before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    // --- Authority Extraction Method (MODIFIED) ---

    /**
     * Extracts authorities (roles) from the JWT token's "roles" claim.
     * Expects the "roles" claim to contain a List of Strings.
     */
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);
        if (claims == null) {
            return Collections.emptyList(); // Return empty if claims couldn't be parsed
        }

        // **MODIFIED: Extract roles from the dedicated "roles" claim**
        Object rolesClaim = claims.get("roles");
        Set<GrantedAuthority> authorities = new HashSet<>();

        if (rolesClaim instanceof List<?> rolesList) {
            rolesList.stream()
                    .filter(role -> role instanceof String) // Ensure elements are strings
                    .map(role -> new SimpleGrantedAuthority((String) role)) // Create authority from string
                    .forEach(authorities::add);
            log.debug("Extracted authorities from token: {}", authorities);
        } else {
            log.warn("Roles claim is missing or not a List in JWT token for subject: {}", claims.getSubject());
        }

        return authorities;
    }


    // getSignInKey remains the same
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}