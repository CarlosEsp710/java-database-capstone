package com.project.back_end.config;

import com.project.back_end.models.Admin;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.services.PasswordService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AdminRepository admins;
    private final PasswordService passwords;
    private final String username;
    private final String password;

    public AdminBootstrap(AdminRepository admins, PasswordService passwords,
                          @Value("${CLINIC_ADMIN_USERNAME:}") String username,
                          @Value("${CLINIC_ADMIN_PASSWORD:}") String password) {
        this.admins = admins;
        this.passwords = passwords;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) return;
        if (username.isBlank() || password.length() < 12)
            throw new IllegalArgumentException("Set CLINIC_ADMIN_USERNAME and a CLINIC_ADMIN_PASSWORD of at least 12 characters");
        Admin admin = admins.findByUsername(username).orElseGet(() -> {
            Admin created = new Admin();
            created.setUsername(username);
            return created;
        });
        if (admin.getPassword() == null || "SEED_ACCOUNT_DISABLED".equals(admin.getPassword())) {
            admin.setPassword(passwords.hash(password));
            admins.save(admin);
        }
    }
}
