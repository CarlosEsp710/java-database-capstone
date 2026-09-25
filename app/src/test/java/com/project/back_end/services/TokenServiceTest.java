package com.project.back_end.services;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.lang.reflect.Proxy;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class TokenServiceTest {

    private static final String SECRET = "a-private-test-only-signing-key-with-at-least-32-bytes";
    private final AdminRepository admins = repository(AdminRepository.class, "findByUsername", "staff", new Admin());
    private final DoctorRepository doctors = repository(DoctorRepository.class, "findByEmail", "doctor@example.com", new Doctor());
    private final TokenService tokens = new TokenService(SECRET, admins, doctors);

    private static <T> T repository(Class<T> type, String lookup, String identity, Object user) {
        return type.cast(Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] {type},
                (proxy, method, args) -> {
                    if (method.getName().equals(lookup)) {
                        return args[0].equals(identity) ? Optional.of(user) : Optional.empty();
                    }
                    throw new UnsupportedOperationException(method.getName());
                }));
    }

    @Test
    void checksSignatureRoleExpiryAndExistingIdentity() {
        String adminToken = tokens.generateToken("staff", "admin");
        String doctorToken = tokens.generateToken("doctor@example.com", "doctor");

        assertTrue(tokens.validateToken(adminToken, "admin"));
        assertTrue(tokens.validateToken(doctorToken, "doctor"));
        assertFalse(tokens.validateToken(adminToken, "doctor"));
        assertFalse(tokens.validateToken(doctorToken, "admin"));
        assertFalse(tokens.validateToken(tokens.generateToken("missing", "doctor"), "doctor"));
        String[] parts = adminToken.split("\\.");
        String tampered = parts[0] + "." + parts[1] + "." + (parts[2].charAt(0) == 'a' ? 'b' : 'a') + parts[2].substring(1);
        assertFalse(tokens.validateToken(tampered, "admin"));
        assertFalse(tokens.validateToken(null, "admin"));
        assertFalse(tokens.validateToken(adminToken, "patient"));

        String expired = Jwts.builder().subject("staff").claim("role", "admin")
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8))).compact();
        assertFalse(tokens.validateToken(expired, "admin"));

        String unsignedRole = Jwts.builder().subject("staff").claim("role", "admin")
                .expiration(Date.from(Instant.now().plusSeconds(60)))
                .signWith(Keys.hmacShaKeyFor("a-different-test-signing-key-with-at-least-32-bytes"
                        .getBytes(StandardCharsets.UTF_8))).compact();
        assertFalse(tokens.validateToken(unsignedRole, "admin"));
    }
}
