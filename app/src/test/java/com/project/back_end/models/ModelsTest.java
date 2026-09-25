package com.project.back_end.models;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.Test;

class ModelsTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void passwordsAreAcceptedButNeverSerialized() throws Exception {
        Admin admin = mapper.readValue("{\"username\":\"staff\",\"password\":\"secret\"}", Admin.class);
        Doctor doctor = mapper.readValue("{\"password\":\"secret\"}", Doctor.class);
        Patient patient = mapper.readValue("{\"password\":\"secret\"}", Patient.class);

        assertEquals("secret", admin.getPassword());
        assertEquals("secret", doctor.getPassword());
        assertEquals("secret", patient.getPassword());
        assertFalse(mapper.writeValueAsString(admin).contains("password"));
        assertFalse(mapper.writeValueAsString(doctor).contains("password"));
        assertFalse(mapper.writeValueAsString(patient).contains("password"));
    }

    @Test
    void appointmentHelpersDeriveTimeWithoutStoredFields() throws Exception {
        Appointment appointment = new Appointment();
        assertNull(appointment.getEndTime());
        appointment.setAppointmentTime(LocalDateTime.of(2030, 5, 1, 9, 30));

        assertEquals(LocalDateTime.of(2030, 5, 1, 10, 30), appointment.getEndTime());
        assertEquals(appointment.getAppointmentTime().toLocalDate(), appointment.getAppointmentDate());
        assertEquals(appointment.getAppointmentTime().toLocalTime(), appointment.getAppointmentTimeOnly());
        assertTrue(Appointment.class.getMethod("getEndTime").isAnnotationPresent(jakarta.persistence.Transient.class));
    }

    @Test
    void validationRejectsInvalidDataAndAcceptsValidPrescription() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            Validator validator = factory.getValidator();
            Doctor doctor = new Doctor();
            doctor.setName("Dr Ada");
            doctor.setSpecialty("Cardiology");
            doctor.setEmail("not-an-email");
            doctor.setPassword("secret");
            doctor.setPhone("123");
            assertTrue(validator.validate(doctor).stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));
            assertTrue(validator.validate(doctor).stream().anyMatch(v -> v.getPropertyPath().toString().equals("phone")));

            Patient patient = new Patient();
            patient.setName("Jordan Smith");
            patient.setEmail("jordan@example.com");
            patient.setPassword("secret");
            patient.setPhone("1234567890");
            patient.setAddress("Main Street");
            assertTrue(validator.validate(patient).isEmpty());

            Appointment appointment = new Appointment();
            appointment.setDoctor(doctor);
            appointment.setPatient(patient);
            appointment.setAppointmentTime(LocalDateTime.now().minusDays(1));
            assertTrue(validator.validate(appointment).stream().anyMatch(v -> v.getPropertyPath().toString().equals("appointmentTime")));

            Prescription prescription = new Prescription("Jordan Smith", "Amoxicillin", "500 mg", null, 51L);
            assertEquals(51L, prescription.getAppointmentId());
            assertTrue(validator.validate(prescription).isEmpty());
            prescription.setDosage(" ");
            assertFalse(validator.validate(prescription).isEmpty());
        }
    }
}
