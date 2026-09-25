package com.project.back_end.DTO;

import com.project.back_end.models.Patient;

public record PatientDTO(Long id, String name, String email, String phone, String address) {
    public PatientDTO(Patient patient) {
        this(patient.getId(), patient.getName(), patient.getEmail(), patient.getPhone(), patient.getAddress());
    }
}
