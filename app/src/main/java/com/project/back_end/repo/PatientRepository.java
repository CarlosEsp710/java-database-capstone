package com.project.back_end.repo;

import com.project.back_end.models.Patient;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByEmail(String email);
    Optional<Patient> findByEmailOrPhone(String email, String phone);
}
