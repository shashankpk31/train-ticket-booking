package com.railway.api_gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

import javax.crypto.SecretKey;

@Component
public class JwtUtil {
	@Value("${security.jwt.secret-key}")
    private String secretString;

    @Value("${security.jwt.expiration}")
    private long expiration; 

    private SecretKey key;

    public void init() {
        this.key = Keys.hmacShaKeyFor(secretString.getBytes());
    }

    public String generateToken(String username, String role) {
        if (this.key == null) {
            init();
        }
        return Jwts.builder()
                .setSubject(username)
                .claim("roles", "ROLE_" + role) 
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key) 
                .compact();
    }

    
    public String extractUsername(String token) {
        // Ensure key is initialized
        if (this.key == null) {
            init();
        }
        Jws<Claims> claimsJws = Jwts.parserBuilder() // Use parserBuilder()
                .setSigningKey(key) // Set the signing key
                .build()
                .parseClaimsJws(token); // Parse the signed JWT
        return claimsJws.getBody().getSubject(); // Get the claims body and then the subject
    }

   
    public String extractRoles(String token) {
        // Ensure key is initialized
        if (this.key == null) {
            init();
        }
        Jws<Claims> claimsJws = Jwts.parserBuilder() // Use parserBuilder()
                .setSigningKey(key) // Set the signing key
                .build()
                .parseClaimsJws(token); // Parse the signed JWT
        return claimsJws.getBody().get("roles", String.class); // Get the claims body and then the "roles" claim
    }

   
    public boolean validateToken(String token) {
        if (this.key == null) {
            init();
        }
        try {
            Jwts.parserBuilder() 
                .setSigningKey(key) 
                .build()
                .parseClaimsJws(token); 
            return true;
        } catch (MalformedJwtException e) {
            System.err.println("Invalid JWT token: " + e.getMessage());
        } catch (ExpiredJwtException e) {
            System.err.println("JWT token is expired: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.err.println("JWT claims string is empty: " + e.getMessage());
        }
        return false;
    }

   
    public boolean isTokenExpired(String token) {
        if (this.key == null) {
            init();
        }
        try {
            Jws<Claims> claimsJws = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return claimsJws.getBody().getExpiration().before(new Date());
        } catch (ExpiredJwtException e) {
            // If it throws ExpiredJwtException, it is indeed expired
            return true;
        } catch (Exception e) {
            // For any other parsing error, consider it expired or invalid for this check
            System.err.println("Error checking token expiration: " + e.getMessage());
            return true;
        }
    }
}