package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.DTO.PatientDTO;
import com.project.back_end.models.Patient;
import com.project.back_end.services.PatientService;
import com.project.back_end.services.Service;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/patient")
public class PatientController {
    private final PatientService patients;
    private final Service service;

    public PatientController(PatientService patients, Service service) {
        this.patients = patients;
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> signup(@Valid @RequestBody Patient patient) {
        patients.createPatient(patient);
        return Map.of("message", "Signup successful");
    }

    @PostMapping("/login")
    public Map<String, String> login(@Valid @RequestBody Login login) {
        return service.validatePatientLogin(login);
    }

    @GetMapping("/{token}")
    public Map<String, PatientDTO> details(@PathVariable String token) {
        return Map.of("patient", patients.getPatientDetails(token));
    }

    @GetMapping("/{id}/{user}/{token}")
    public Map<String, List<AppointmentDTO>> appointments(@PathVariable Long id, @PathVariable String user,
                                                            @PathVariable String token) {
        return Map.of("appointments", switch (user) {
            case "patient" -> patients.getPatientAppointment(id, token);
            case "doctor" -> patients.getDoctorPatientAppointment(id, token);
            default -> throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown role");
        });
    }

    @GetMapping("/{id}/{token}")
    public Map<String, List<AppointmentDTO>> patientAppointments(@PathVariable Long id, @PathVariable String token) {
        return Map.of("appointments", patients.getPatientAppointment(id, token));
    }

    @GetMapping("/filter/{condition}/{name}/{token}")
    public Map<String, List<AppointmentDTO>> filter(@PathVariable String condition, @PathVariable String name,
                                                     @PathVariable String token) {
        return Map.of("appointments", patients.filter(condition, name, token));
    }
}
