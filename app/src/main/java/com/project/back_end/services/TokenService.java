package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class TokenService {

    private final SecretKey signingKey;
    private final AdminRepository admins;
    private final DoctorRepository doctors;

    public TokenService(@Value("${jwt.secret}") String secret, AdminRepository admins, DoctorRepository doctors) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.admins = admins;
        this.doctors = doctors;
    }

    public String generateToken(String subject, String role) {
        if (subject == null || subject.isBlank() || (!"admin".equals(role) && !"doctor".equals(role))) {
            throw new IllegalArgumentException("A supported role and subject are required");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(subject)
                .claim("role", role)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(7, ChronoUnit.DAYS)))
                .signWith(signingKey)
                .compact();
    }

    public boolean validateToken(String token, String role) {
        if (token == null || token.isBlank() || (!"admin".equals(role) && !"doctor".equals(role))) {
            return false;
        }
        final Claims claims;
        try {
            claims = Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank() || !role.equals(claims.get("role", String.class))
                || claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            return false;
        }
        return switch (role) {
            case "admin" -> admins.findByUsername(subject).isPresent();
            case "doctor" -> doctors.findByEmail(subject).isPresent();
            default -> false;
        };
    }
}
