package com.project.back_end.controllers;

import com.project.back_end.DTO.DoctorDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Doctor;
import com.project.back_end.services.DoctorService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.path}doctor")
public class DoctorController {
    private final DoctorService doctors;
    private final Service service;

    public DoctorController(DoctorService doctors, Service service) {
        this.doctors = doctors;
        this.service = service;
    }

    @GetMapping
    public Map<String, List<DoctorDTO>> all() { return Map.of("doctors", doctors.getDoctors()); }

    @GetMapping("/filter/{name}/{time}/{speciality}")
    public Map<String, List<DoctorDTO>> filter(@PathVariable String name, @PathVariable String time,
                                                @PathVariable String speciality) {
        return Map.of("doctors", doctors.filterDoctors(name, speciality, time));
    }

    @GetMapping("/availability/{user}/{doctorId}/{date}/{token}")
    public Map<String, List<String>> availability(@PathVariable String user, @PathVariable Long doctorId,
                                                   @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                                                   @PathVariable String token) {
        if (!List.of("patient", "doctor", "admin").contains(user))
            throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role");
        service.requireRole(token, user);
        return Map.of("availability", doctors.getDoctorAvailability(doctorId, date));
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Login login) { return doctors.validateDoctor(login); }

    @PostMapping("/{token}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@PathVariable String token, @Valid @RequestBody Doctor doctor) {
        service.requireRole(token, "admin");
        doctors.saveDoctor(doctor);
        return Map.of("message", "Doctor added to db");
    }

    @PutMapping("/{token}")
    public Map<String, String> update(@PathVariable String token, @Valid @RequestBody Doctor doctor) {
        service.requireRole(token, "admin");
        doctors.updateDoctor(doctor);
        return Map.of("message", "Doctor updated");
    }

    @DeleteMapping("/{id}/{token}")
    public Map<String, String> delete(@PathVariable long id, @PathVariable String token) {
        service.requireRole(token, "admin");
        doctors.deleteDoctor(id);
        return Map.of("message", "Doctor deleted successfully");
    }
}
