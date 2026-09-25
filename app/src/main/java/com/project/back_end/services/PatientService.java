package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.DTO.PatientDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PatientRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PatientService {
    private final PatientRepository patients;
    private final AppointmentRepository appointments;
    private final PasswordService passwords;
    private final TokenService tokens;

    public PatientService(PatientRepository patients, AppointmentRepository appointments,
                          PasswordService passwords, TokenService tokens) {
        this.patients = patients;
        this.appointments = appointments;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @Transactional
    public PatientDTO createPatient(Patient patient) {
        if (patient.getId() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Do not supply a patient ID");
        if (patients.findByEmailOrPhone(patient.getEmail(), patient.getPhone()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Patient with email id or phone no already exist");
        patient.setPassword(passwords.hash(patient.getPassword()));
        return new PatientDTO(patients.saveAndFlush(patient));
    }

    @Transactional(readOnly = true)
    public PatientDTO getPatientDetails(String token) {
        return new PatientDTO(patient(token));
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getPatientAppointment(Long id, String token) {
        Patient patient = patient(token);
        if (!patient.getId().equals(id)) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your appointments");
        return appointments.findByPatientId(id).stream().map(AppointmentDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getDoctorPatientAppointment(Long id, String token) {
        String email = tokens.extractIdentifier(token, "doctor");
        return appointments.findByPatientId(id).stream()
                .filter(a -> a.getDoctor().getEmail().equals(email))
                .map(AppointmentDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> filter(String condition, String name, String token) {
        Patient patient = patient(token);
        List<Appointment> result = appointments.findByPatientId(patient.getId());
        if (name != null && !name.isBlank() && !"null".equalsIgnoreCase(name))
            result = result.stream().filter(a -> a.getDoctor().getName().toLowerCase()
                    .contains(name.toLowerCase())).toList();
        if (condition != null && !condition.isBlank() && !"null".equalsIgnoreCase(condition)) {
            result = switch (condition.toLowerCase()) {
                case "past" -> result.stream().filter(a -> a.getStatus() == 1).toList();
                case "future", "upcoming" -> result.stream().filter(a -> a.getStatus() == 0).toList();
                default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown appointment filter");
            };
        }
        return result.stream().map(AppointmentDTO::new).toList();
    }

    private Patient patient(String token) {
        return patients.findByEmail(tokens.extractIdentifier(token, "patient"))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid patient"));
    }
}
