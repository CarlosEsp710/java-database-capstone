package com.project.back_end.services;

import com.project.back_end.models.Appointment;
import com.project.back_end.models.Prescription;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.PrescriptionRepository;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptions;
    private final AppointmentRepository appointments;
    private final TokenService tokens;

    public PrescriptionService(PrescriptionRepository prescriptions, AppointmentRepository appointments, TokenService tokens) {
        this.prescriptions = prescriptions;
        this.appointments = appointments;
        this.tokens = tokens;
    }

    public Prescription savePrescription(Prescription prescription, String token) {
        Appointment appointment = ownedAppointment(prescription.getAppointmentId(), token);
        if (prescription.getId() != null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Do not supply a prescription ID");
        if (!appointment.getPatient().getName().equals(prescription.getPatientName()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patient does not match appointment");
        return prescriptions.save(prescription);
    }

    public List<Prescription> getPrescription(Long appointmentId, String token) {
        ownedAppointment(appointmentId, token);
        return prescriptions.findByAppointmentId(appointmentId);
    }

    private Appointment ownedAppointment(Long appointmentId, String token) {
        String doctorEmail = tokens.extractIdentifier(token, "doctor");
        if (appointmentId == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Appointment ID is required");
        Appointment appointment = appointments.findById(appointmentId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Appointment not found"));
        if (!appointment.getDoctor().getEmail().equals(doctorEmail))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your appointment");
        return appointment;
    }
}
