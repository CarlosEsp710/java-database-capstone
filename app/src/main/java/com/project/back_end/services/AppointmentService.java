package com.project.back_end.services;

import com.project.back_end.DTO.AppointmentDTO;
import com.project.back_end.models.Appointment;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AppointmentService {
    private final AppointmentRepository appointments;
    private final DoctorRepository doctors;
    private final PatientRepository patients;
    private final TokenService tokens;

    public AppointmentService(AppointmentRepository appointments, DoctorRepository doctors,
                              PatientRepository patients, TokenService tokens) {
        this.appointments = appointments;
        this.doctors = doctors;
        this.patients = patients;
        this.tokens = tokens;
    }

    @Transactional
    public AppointmentDTO bookAppointment(Appointment request, String token) {
        String email = tokens.extractIdentifier(token, "patient");
        if (request.getId() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Do not supply an appointment ID");
        Patient patient = patientFor(email, request);
        Doctor doctor = lockDoctor(request);
        checkSlot(doctor, request.getAppointmentTime(), null);
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment.setAppointmentTime(request.getAppointmentTime());
        appointment.setStatus(0);
        return new AppointmentDTO(appointments.saveAndFlush(appointment));
    }

    @Transactional
    public AppointmentDTO updateAppointment(Appointment request, String token) {
        String email = tokens.extractIdentifier(token, "patient");
        if (request.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment ID is required");
        // Lock the doctor before inspecting bookings so concurrent requests cannot take the same slot.
        Doctor doctor = lockDoctor(request);
        Appointment appointment = appointments.findById(request.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (!appointment.getPatient().getEmail().equals(email)
                || request.getPatient() == null || !appointment.getPatient().getId().equals(request.getPatient().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your appointment");
        if (appointment.getStatus() != 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only scheduled appointments can be changed");
        checkSlot(doctor, request.getAppointmentTime(), appointment.getId());
        appointment.setDoctor(doctor);
        appointment.setAppointmentTime(request.getAppointmentTime());
        return new AppointmentDTO(appointments.saveAndFlush(appointment));
    }

    @Transactional
    public void cancelAppointment(long id, String token) {
        String email = tokens.extractIdentifier(token, "patient");
        Appointment appointment = appointments.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (!appointment.getPatient().getEmail().equals(email))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your appointment");
        if (appointment.getStatus() != 0)
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Only scheduled appointments can be cancelled");
        appointment.setStatus(2);
        appointments.save(appointment);
    }

    @Transactional(readOnly = true)
    public List<AppointmentDTO> getAppointment(String patientName, LocalDate date, String token) {
        String email = tokens.extractIdentifier(token, "doctor");
        Doctor doctor = doctors.findByEmail(email).orElseThrow();
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        List<Appointment> result = patientName == null || patientName.isBlank() || "null".equalsIgnoreCase(patientName)
                ? appointments.findByDoctorIdAndAppointmentTimeBetween(doctor.getId(), start, end)
                : appointments.findByDoctorIdAndPatient_NameContainingIgnoreCaseAndAppointmentTimeBetween(
                        doctor.getId(), patientName, start, end);
        return result.stream().map(AppointmentDTO::new).toList();
    }

    private Patient patientFor(String email, Appointment request) {
        Patient patient = patients.findByEmail(email).orElseThrow();
        if (request.getPatient() == null || !patient.getId().equals(request.getPatient().getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your appointment");
        return patient;
    }

    private Doctor lockDoctor(Appointment request) {
        if (request.getDoctor() == null || request.getDoctor().getId() == null)
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor ID is required");
        return doctors.lockById(request.getDoctor().getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor not found"));
    }

    private void checkSlot(Doctor doctor, LocalDateTime time, Long excludingId) {
        if (time == null || !time.isAfter(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Choose a future appointment");
        String slot = time.toLocalTime().toString() + "-" + time.plusHours(1).toLocalTime();
        if (!doctor.getAvailableTimes().contains(slot))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Doctor is unavailable at that time");
        List<Appointment> sameDay = appointments.findByDoctorIdAndAppointmentTimeBetween(
                doctor.getId(), time.toLocalDate().atStartOfDay(), time.toLocalDate().plusDays(1).atStartOfDay());
        if (sameDay.stream().anyMatch(a -> a.getStatus() != 2 && !a.getId().equals(excludingId)
                && a.getAppointmentTime().isBefore(time.plusHours(1))
                && time.isBefore(a.getAppointmentTime().plusHours(1))))
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Appointment already booked");
    }
}
