package com.project.back_end.controllers;

import com.project.back_end.models.Prescription;
import com.project.back_end.services.PrescriptionService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${api.path}prescription")
public class PrescriptionController {
    private final PrescriptionService prescriptions;

    public PrescriptionController(PrescriptionService prescriptions) { this.prescriptions = prescriptions; }

    @PostMapping("/{token}")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> create(@PathVariable String token, @Valid @RequestBody Prescription prescription) {
        prescriptions.savePrescription(prescription, token);
        return Map.of("message", "Prescription saved");
    }

    @GetMapping("/{appointmentId}/{token}")
    public Map<String, List<Prescription>> get(@PathVariable Long appointmentId, @PathVariable String token) {
        return Map.of("prescription", prescriptions.getPrescription(appointmentId, token));
    }
}
