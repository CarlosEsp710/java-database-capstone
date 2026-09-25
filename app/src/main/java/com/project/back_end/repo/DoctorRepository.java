package com.project.back_end.repo;

import com.project.back_end.models.Doctor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    @Override
    @EntityGraph(attributePaths = "availableTimes")
    List<Doctor> findAll();

    Optional<Doctor> findByEmail(String email);
    List<Doctor> findByNameContainingIgnoreCase(String name);
    List<Doctor> findByNameContainingIgnoreCaseAndSpecialtyIgnoreCase(String name, String specialty);
    List<Doctor> findBySpecialtyIgnoreCase(String specialty);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select d from Doctor d where d.id = :id")
    Optional<Doctor> lockById(@org.springframework.data.repository.query.Param("id") Long id);
}
