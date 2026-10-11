
package ecommerce.back.security;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import ecommerce.back.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

        private final SecretKey secretKey;

        public JwtService(@Value("${jwt.secret}") String secret) {
                byte[] keyBytes = Decoders.BASE64.decode(secret);
                this.secretKey = Keys.hmacShaKeyFor(keyBytes);
        }

        // Generate JWT token
        public String generateToken(User user) {

                long currentTime = System.currentTimeMillis();

                return Jwts.builder()
                                .subject(user.getEmail())
                                .claim("role", user.getRole())
                                .issuedAt(new Date(currentTime))
                                .expiration(new Date(currentTime + 1000L * 60 * 60 * 24 * 7))
                                .signWith(secretKey)
                                .compact();
        }

        // Extract email
        public String extractUsername(String token) {
                return extractClaims(token).getSubject();
        }

        // Extract role
        public String extractRole(String token) {
                return extractClaims(token).get("role", String.class);
        }

        // Validate token signature and expiration
        public boolean isTokenValid(String token) {
                try {
                        extractClaims(token);
                        return true;
                } catch (Exception e) {
                        return false;
                }
        }

        // Parse and verify token
        private Claims extractClaims(String token) {
                return Jwts.parser()
                                .verifyWith(secretKey)
                                .build()
                                .parseSignedClaims(token)
                                .getPayload();
        }
}
