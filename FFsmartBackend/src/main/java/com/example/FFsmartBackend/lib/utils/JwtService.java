package com.example.FFsmartBackend.lib.utils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

/**
 * JwtService handles generation and validation of JWT tokens using a secret key.
 */
@Service
public class JwtService {

    // Secret string used for signing JWTs, injected from application.properties or application.yml
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    // How long the token should last (in ms), also injected from configuration.
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * Returns a Java Security Key for HMAC using the secret.
     * @return Key for signing JWTs
     */
    private Key getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Utility method to hash the secret key for logging or debugging.
     * @return hashed secret key as a hex string
     */
    private String getSecretKeyHash() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(jwtSecret.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedhash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Converts the byte array of the hashed secret into a hex representation.
     */
    private static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Generates a JWT token for the given userId and role, with an expiration time.
     * @param userId The ID of the user
     * @param role The role of the user (e.g., "Manager", "HeadChef")
     * @return a signed JWT token
     */
    public String generateToken(String userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        // For debugging, print out a hash of the secret key
        System.out.println("Secret Key Hash (Generation): " + getSecretKeyHash());

        // Build and return the signed token with userId and role
        return Jwts.builder()
                .setSubject(userId)
                .claim("role", role) // Store role in JWT
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Retrieves the expiration time from configuration.
     */
    public long getJwtExpirationMs() {
        return jwtExpirationMs;
    }

    /**
     * Validates the JWT and returns the user ID (subject) if valid.
     * @param token the JWT token
     * @return userId (subject) if valid, otherwise null
     */
    public String validateTokenAndGetUserId(String token) {
        try {
            // For debugging, print out a hash of the secret key
            System.out.println("Secret Key Hash (Validation): " + getSecretKeyHash());

            // Parse token with the same signing key
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            System.out.println("Token issued at (iat): " + claims.getIssuedAt());
            System.out.println("Token expiration (exp): " + claims.getExpiration());
            System.out.println("Current date: " + new Date());

            // Return the subject, which is the user ID
            return claims.getSubject();
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            // Token is expired
            System.out.println("Token has expired");
            return null;
        } catch (JwtException | IllegalArgumentException e) {
            // Any other parsing or validation issues
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Extracts the user's role from a valid JWT.
     * @param token the JWT token
     * @return the role of the user if valid, otherwise null
     */
    public String getRoleFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get("role", String.class); // Extract role from token
        } catch (JwtException e) {
            return null;
        }
    }
}
