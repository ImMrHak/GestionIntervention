package com.gestion.intervention.kernel.security.jwt;

import com.gestion.intervention.domain.person.model.Person;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.*;
import java.util.function.Function;

// The class is marked as a Spring Service, which will be automatically injected into other components of the Spring application.
@Service
public class JwtService {

    // Inject the secret key from application properties (typically used for signing JWT tokens)
    @Value("${security.jwt.secret-key}")
    private String secretKey;

    // Inject the expiration time for the access token from application properties (in milliseconds)
    @Value("${security.jwt.access-token-expiration-time}")
    private long jwtAccessExpiration;

    // Inject the expiration time for the refresh token from application properties (in milliseconds)
    @Value("${security.jwt.refresh-token-expiration-time}")
    private long jwtRefreshExpiration;

    // Extract the username (subject) from the JWT token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Extract the user id (userId) from the JWT token
    public UUID extractUserId(String token) {
        String userIdStr = extractClaim(token, claims -> claims.get("userId", String.class));
        return userIdStr != null ? UUID.fromString(userIdStr) : null;
    }

    // Extract a specific claim from the JWT token. A claim is a piece of information encoded in the token (e.g., subject, expiration time).
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        // Extract all claims from the token and then apply the provided claimsResolver function to extract the desired claim.
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Generate an access token for a given user by extracting their authorities (roles) and setting them as extra claims.
    public String generateAccessToken(Person userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Loop through the user's authorities (roles) and add them as extra claims.
        userDetails.getAuthorities().forEach(a -> extraClaims.put(a.getAuthority(), a));
        // Add a custom claim to indicate the user id without retrieving it from the database
        extraClaims.put("userId", userDetails.getId().toString());
        // Call generateToken to create the JWT with the specified expiration for access tokens.
        return generateToken(extraClaims, userDetails, jwtAccessExpiration);
    }

    // Generate a refresh token for a given user by adding a special claim to mark it as a refresh token.
    public String generateRefreshToken(Person userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Add a custom claim to indicate the user id without retrieving it from the database
        extraClaims.put("userId", userDetails.getId().toString());
        // Add a custom claim to indicate that the token is a refresh token.
        extraClaims.put("type", "refresh");
        // Call generateToken to create the JWT with the specified expiration for refresh tokens.
        return generateToken(extraClaims, userDetails, jwtRefreshExpiration);
    }

    // Common method to generate a JWT with extra claims, user details, and expiration time.
    private String generateToken(Map<String, Object> extraClaims, Person userDetails, long expiration) {
        return buildToken(extraClaims, userDetails, expiration);
    }

    // Build the JWT token using the provided claims, user details, and expiration time.
    private String buildToken(Map<String, Object> extraClaims, Person userDetails, long expiration) {
        return Jwts
                .builder()  // Start building the JWT
                .setClaims(extraClaims)  // Set the extra claims in the token
                .setSubject(userDetails.getUsername())  // Set the subject (username) as the JWT subject
                .setIssuedAt(new Date(System.currentTimeMillis()))  // Set the issue date as the current date and time
                .setExpiration(new Date(System.currentTimeMillis() + expiration))  // Set the expiration date by adding the expiration time to the current time
                .signWith(getSignInKey(), SignatureAlgorithm.HS256)  // Sign the token with the secret key using HMAC SHA-256
                .compact();  // Create and return the compact JWT token string
    }

    // Check if the token is valid by comparing the token's username with the provided username and ensuring it's not expired.
    public boolean isTokenValid(String token, String username) {
        final String tokenUsername = extractUsername(token);  // Extract the username from the token
        final boolean isTokenExpired = isTokenExpired(token);  // Check if the token is expired

        // Return true if the token's username matches the provided username and the token is not expired.
        return tokenUsername.equals(username) && !isTokenExpired;
    }

    // Check if the token is expired by comparing the token's expiration date with the current date and time.
    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());  // Check if the token's expiration date is before the current date
    }

    // Extract the expiration date from the token.
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);  // Extract the expiration date claim
    }

    // Extract the authorities (roles/permissions) from the JWT token and convert them into a collection of GrantedAuthority objects.
    public Collection<? extends GrantedAuthority> extractAuthorities(String token) {
        Claims claims = extractAllClaims(token);

        Set<GrantedAuthority> authorities = new HashSet<>();

        // Extract roles (keys starting with "ROLE_")
        claims.forEach((key, value) -> {
            if (key.startsWith("ROLE_") && value instanceof Map<?, ?> roleData) {
                authorities.add(new SimpleGrantedAuthority(key)); // Add role itself

                // Extract privileges from role
                Object privilegesObj = roleData.get("privileges");
                if (privilegesObj instanceof List<?> privilegesList) {
                    privilegesList.forEach(privilege -> {
                        if (privilege instanceof Map<?, ?> privilegeData) {
                            Object privilegeAuthority = privilegeData.get("authority");
                            if (privilegeAuthority instanceof String authority) {
                                authorities.add(new SimpleGrantedAuthority(authority));
                            }
                        }
                    });
                }
            }
        });

        return authorities;
    }

    // Extract all claims from the token by parsing it using the secret key.
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()  // Start parsing the JWT
                .setSigningKey(getSignInKey())  // Set the signing key used to verify the token
                .build()
                .parseClaimsJws(token)  // Parse the token
                .getBody();  // Return the claims from the parsed token
    }

    // Convert the secret key from a base64 encoded string to a Key object used for signing and verifying JWT tokens.
    private Key getSignInKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);  // Decode the secret key from base64
        return Keys.hmacShaKeyFor(keyBytes);  // Return a signing key for HMAC SHA
    }
}
