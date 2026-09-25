package com.project.back_end.controllers;

import com.project.back_end.DTO.Login;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.path}admin")
public class AdminController {
    private final Service service;

    public AdminController(Service service) { this.service = service; }

    @PostMapping
    public Map<String, String> login(@Valid @RequestBody Login login) {
        return service.validateAdmin(login);
    }
}
