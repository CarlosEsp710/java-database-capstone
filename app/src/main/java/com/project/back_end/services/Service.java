package com.project.back_end.services;

import com.project.back_end.DTO.Login;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.PatientRepository;
import java.util.Map;

@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;
    private final AdminRepository admins;
    private final PasswordService passwords;
    private final PatientRepository patients;

    public Service(TokenService tokenService, AdminRepository admins, PasswordService passwords, PatientRepository patients) {
        this.tokenService = tokenService;
        this.admins = admins;
        this.passwords = passwords;
        this.patients = patients;
    }

    public Map<String, String> validateToken(String token, String role) {
        if (tokenService.validateToken(token, role)) {
            return Map.of();
        }
        return Map.of("error", "Invalid or expired token");
    }

    public String requireRole(String token, String role) {
        return tokenService.extractIdentifier(token, role);
    }

    public Map<String, String> validateAdmin(Login login) {
        var admin = admins.findByUsername(login.getIdentifier())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwords.matches(login.getPassword(), admin.getPassword()))
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return Map.of("token", tokenService.generateToken(admin.getUsername(), "admin"));
    }

    public Map<String, String> validatePatientLogin(Login login) {
        var patient = patients.findByEmail(login.getIdentifier())
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(
                        org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwords.matches(login.getPassword(), patient.getPassword()))
            throw new org.springframework.web.server.ResponseStatusException(
                    org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return Map.of("token", tokenService.generateToken(patient.getEmail(), "patient"));
    }
}
