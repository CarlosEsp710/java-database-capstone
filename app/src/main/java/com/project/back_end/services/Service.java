package com.project.back_end.services;

import java.util.Map;
@org.springframework.stereotype.Service
public class Service {

    private final TokenService tokenService;

    public Service(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    public Map<String, String> validateToken(String token, String role) {
        if (tokenService.validateToken(token, role)) {
            return Map.of();
        }
        return Map.of("error", "Invalid or expired token");
    }
}
