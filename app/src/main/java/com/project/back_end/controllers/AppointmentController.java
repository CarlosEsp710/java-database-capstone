package com.project.back_end.controllers;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.services.AppointmentService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {
    private final AppointmentService appointments;

    public AppointmentController(AppointmentService appointments) { this.appointments = appointments; }

    @GetMapping("/{date}/{patientName}/{token}")
    public Map<String, List<AppointmentDTO>> forDoctor(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @PathVariable String patientName, @PathVariable String token) {
        return Map.of("appointments", appointments.getAppointment(patientName, date, token));
    }

    @PostMapping("/{token}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> book(@PathVariable String token, @Valid @RequestBody Appointment appointment) {
        appointments.bookAppointment(appointment, token);
        return Map.of("message", "Appointment booked");
    }

    @PutMapping("/{token}")
    public Map<String, String> update(@PathVariable String token, @Valid @RequestBody Appointment appointment) {
        appointments.updateAppointment(appointment, token);
        return Map.of("message", "Appointment updated");
    }

    @DeleteMapping("/{id}/{token}")
    public Map<String, String> cancel(@PathVariable long id, @PathVariable String token) {
        appointments.cancelAppointment(id, token);
        return Map.of("message", "Appointment cancelled");
    }
}
