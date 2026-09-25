package com.project.back_end.DTO;

import com.project.back_end.models.Appointment;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AppointmentDTO(Long id, Long doctorId, String doctorName, Long patientId,
                             String patientName, String patientEmail, String patientPhone,
                             String patientAddress, LocalDateTime appointmentTime, int status,
                             LocalDate appointmentDate, LocalTime appointmentTimeOnly,
                             LocalDateTime endTime) {
    public AppointmentDTO(Appointment appointment) {
        this(appointment.getId(), appointment.getDoctor().getId(), appointment.getDoctor().getName(),
                appointment.getPatient().getId(), appointment.getPatient().getName(),
                appointment.getPatient().getEmail(), appointment.getPatient().getPhone(),
                appointment.getPatient().getAddress(), appointment.getAppointmentTime(),
                appointment.getStatus(), appointment.getAppointmentTime().toLocalDate(),
                appointment.getAppointmentTime().toLocalTime(), appointment.getAppointmentTime().plusHours(1));
    }
}
