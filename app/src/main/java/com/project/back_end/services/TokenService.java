package com.project.back_end.services;

import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
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
    private final PatientRepository patients;

    public TokenService(@Value("${jwt.secret}") String secret, AdminRepository admins, DoctorRepository doctors,
                        PatientRepository patients) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.admins = admins;
        this.doctors = doctors;
        this.patients = patients;
    }

    public String generateToken(String subject, String role) {
        if (subject == null || subject.isBlank() || (!"admin".equals(role) && !"doctor".equals(role) && !"patient".equals(role))) {
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
        if (token == null || token.isBlank() || (!"admin".equals(role) && !"doctor".equals(role) && !"patient".equals(role))) {
            return false;
        }
        Claims claims = parse(token);
        if (claims == null) return false;
        String subject = claims.getSubject();
        if (subject == null || subject.isBlank() || !role.equals(claims.get("role", String.class))) return false;
        return switch (role) {
            case "admin" -> admins.findByUsername(subject).isPresent();
            case "doctor" -> doctors.findByEmail(subject).isPresent();
            case "patient" -> patients.findByEmail(subject).isPresent();
            default -> false;
        };
    }

    public String extractIdentifier(String token, String role) {
        if (!validateToken(token, role)) {
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }
        return parse(token).getSubject();
    }

    private Claims parse(String token) {
        if (token == null || token.isBlank()) return null;
        try {
            Claims claims = Jwts.parser().verifyWith(signingKey).build()
                    .parseSignedClaims(token).getPayload();
            if (claims.getExpiration() == null || !claims.getExpiration().after(new Date())) return null;
            return claims;
        } catch (JwtException | IllegalArgumentException e) {
            return null;
        }
    }
}
