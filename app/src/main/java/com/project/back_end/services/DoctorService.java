package com.project.back_end.services;

import com.project.back_end.DTO.DoctorDTO;
import com.project.back_end.DTO.Login;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class DoctorService {
    private final DoctorRepository doctors;
    private final AppointmentRepository appointments;
    private final PasswordService passwords;
    private final TokenService tokens;

    public DoctorService(DoctorRepository doctors, AppointmentRepository appointments,
                         PasswordService passwords, TokenService tokens) {
        this.doctors = doctors;
        this.appointments = appointments;
        this.passwords = passwords;
        this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> getDoctors() {
        return doctors.findAll().stream().map(DoctorDTO::new).toList();
    }

    @Transactional(readOnly = true)
    public List<DoctorDTO> filterDoctors(String name, String specialty, String time) {
        return doctors.findAll().stream()
                .filter(d -> empty(name) || d.getName().toLowerCase().contains(name.toLowerCase()))
                .filter(d -> empty(specialty) || d.getSpecialty().equalsIgnoreCase(specialty))
                .filter(d -> empty(time) || d.getAvailableTimes().stream().anyMatch(slot -> matchesTime(slot, time)))
                .map(DoctorDTO::new).toList();
    }

    private boolean empty(String value) {
        return value == null || value.isBlank() || "null".equalsIgnoreCase(value);
    }

    private boolean matchesTime(String slot, String filter) {
        if ("AM".equalsIgnoreCase(filter) || "PM".equalsIgnoreCase(filter)) {
            try {
                return (LocalTime.parse(slot.substring(0, 5)).getHour() < 12) == "AM".equalsIgnoreCase(filter);
            } catch (java.time.format.DateTimeParseException | IndexOutOfBoundsException e) {
                return false;
            }
        }
        return slot.equals(filter);
    }

    @Transactional(readOnly = true)
    public List<String> getDoctorAvailability(Long id, LocalDate date) {
        Doctor doctor = doctors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        List<Appointment> booked = appointments.findByDoctorIdAndAppointmentTimeBetween(id,
                date.atStartOfDay(), date.plusDays(1).atStartOfDay());
        return doctor.getAvailableTimes().stream().filter(slot -> booked.stream().noneMatch(a ->
                a.getStatus() != 2 && slot.startsWith(a.getAppointmentTime().toLocalTime().toString())))
                .toList();
    }

    @Transactional
    public DoctorDTO saveDoctor(Doctor doctor) {
        if (doctor.getId() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Do not supply a doctor ID");
        if (doctors.findByEmail(doctor.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already exists");
        doctor.setPassword(passwords.hash(doctor.getPassword()));
        return new DoctorDTO(doctors.saveAndFlush(doctor));
    }

    @Transactional
    public DoctorDTO updateDoctor(Doctor input) {
        Doctor doctor = doctors.findById(input.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        if (!doctor.getEmail().equals(input.getEmail()) && doctors.findByEmail(input.getEmail()).isPresent())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor already exists");
        doctor.setName(input.getName());
        doctor.setSpecialty(input.getSpecialty());
        doctor.setEmail(input.getEmail());
        doctor.setPhone(input.getPhone());
        doctor.setAvailableTimes(input.getAvailableTimes());
        if (input.getPassword() != null && !input.getPassword().isBlank())
            doctor.setPassword(passwords.hash(input.getPassword()));
        return new DoctorDTO(doctors.saveAndFlush(doctor));
    }

    @Transactional
    public void deleteDoctor(long id) {
        Doctor doctor = doctors.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
        if (!appointments.findByDoctorIdAndAppointmentTimeBetween(id,
                LocalDate.of(1970, 1, 1).atStartOfDay(), LocalDate.of(9999, 1, 1).atStartOfDay()).isEmpty())
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Doctor has appointment history");
        doctors.delete(doctor);
    }

    @Transactional(readOnly = true)
    public Map<String, String> validateDoctor(Login login) {
        Doctor doctor = doctors.findByEmail(login.getIdentifier())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwords.matches(login.getPassword(), doctor.getPassword()))
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        return Map.of("token", tokens.generateToken(doctor.getEmail(), "doctor"));
    }
}
