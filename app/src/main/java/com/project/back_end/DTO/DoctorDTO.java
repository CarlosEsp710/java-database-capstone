package com.project.back_end.DTO;

import com.project.back_end.models.Doctor;
import java.util.List;

public record DoctorDTO(Long id, String name, String specialty, String email, String phone,
                        List<String> availableTimes) {
    public DoctorDTO(Doctor doctor) {
        this(doctor.getId(), doctor.getName(), doctor.getSpecialty(), doctor.getEmail(),
                doctor.getPhone(), List.copyOf(doctor.getAvailableTimes()));
    }
}
