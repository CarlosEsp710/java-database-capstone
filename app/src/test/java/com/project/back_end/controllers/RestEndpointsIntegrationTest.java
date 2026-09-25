package com.project.back_end.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.project.back_end.models.Admin;
import com.project.back_end.config.AdminBootstrap;
import com.project.back_end.models.Doctor;
import com.project.back_end.models.Patient;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.AppointmentRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.repo.PatientRepository;
import com.project.back_end.services.PasswordService;
import com.project.back_end.services.TokenService;
import com.project.testsupport.PrescriptionTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PrescriptionTestConfiguration.class)
class RestEndpointsIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired AdminRepository admins;
    @Autowired DoctorRepository doctors;
    @Autowired PatientRepository patients;
    @Autowired PasswordService passwords;
    @Autowired TokenService tokens;
    @Autowired AppointmentRepository appointments;

    @Test
    @Transactional
    void loginAndRoleRestrictions() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("rest-admin");
        admin.setPassword(passwords.hash("a-test-password"));
        admins.saveAndFlush(admin);
        String adminToken = tokens.generateToken(admin.getUsername(), "admin");

        mvc.perform(post("/admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"rest-admin\",\"password\":\"a-test-password\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        mvc.perform(post("/admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"rest-admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/doctor/invalid").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Valid Doctor\",\"specialty\":\"Cardiology\",\"email\":\"rest@clinic.example\",\"phone\":\"1234567890\",\"password\":\"valid-pass\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/doctor/" + adminToken).contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Valid Doctor\",\"specialty\":\"Cardiology\",\"email\":\"rest@clinic.example\",\"phone\":\"1234567890\",\"password\":\"valid-pass\",\"availableTimes\":[\"09:00-10:00\"]}"))
                .andExpect(status().isCreated());
        mvc.perform(get("/doctor/filter/null/09:00-10:00/Cardiology"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.doctors").isArray());
        mvc.perform(post("/doctor/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"rest@clinic.example\",\"password\":\"valid-pass\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        mvc.perform(get("/doctor")).andExpect(status().isOk())
                .andExpect(jsonPath("$.doctors[0].password").doesNotExist());
    }

    @Test
    @Transactional
    void bootstrapOnlyActivatesDisabledAdmin() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("rest-seed-admin");
        admin.setPassword("SEED_ACCOUNT_DISABLED");
        admins.saveAndFlush(admin);
        new AdminBootstrap(admins, passwords, admin.getUsername(), "a-new-strong-password").run(null);
        mvc.perform(post("/admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"rest-seed-admin\",\"password\":\"a-new-strong-password\"}"))
                .andExpect(status().isOk());
        new AdminBootstrap(admins, passwords, admin.getUsername(), "a-different-password").run(null);
        mvc.perform(post("/admin").contentType(MediaType.APPLICATION_JSON)
                .content("{\"username\":\"rest-seed-admin\",\"password\":\"a-different-password\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Transactional
    void signupBookAndPatientIsolation() throws Exception {
        Doctor doctor = new Doctor();
        doctor.setName("Appointment Doctor");
        doctor.setSpecialty("Cardiology");
        doctor.setEmail("booking@clinic.example");
        doctor.setPassword(passwords.hash("doctor-pass"));
        doctor.setPhone("1234567890");
        doctor.setAvailableTimes(java.util.List.of("09:00-10:00"));
        doctors.saveAndFlush(doctor);
        mvc.perform(post("/patient").contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"Test Patient\",\"email\":\"booking-patient@clinic.example\",\"password\":\"patient-pass\",\"phone\":\"2223334444\",\"address\":\"12 Example St\"}"))
                .andExpect(status().isCreated());
        Patient patient = patients.findByEmail("booking-patient@clinic.example").orElseThrow();
        String token = tokens.generateToken(patient.getEmail(), "patient");
        String doctorToken = tokens.generateToken(doctor.getEmail(), "doctor");
        mvc.perform(post("/patient/login").contentType(MediaType.APPLICATION_JSON)
                .content("{\"email\":\"booking-patient@clinic.example\",\"password\":\"patient-pass\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").isString());
        mvc.perform(get("/patient/" + token)).andExpect(status().isOk())
                .andExpect(jsonPath("$.patient.password").doesNotExist());
        String date = java.time.LocalDate.now().plusDays(10).toString();
        String appointment = "{\"doctor\":{\"id\":" + doctor.getId() + "},\"patient\":{\"id\":"
                + patient.getId() + "},\"appointmentTime\":\"" + date + "T09:00:00\",\"status\":0}";
        mvc.perform(post("/appointments/" + doctorToken).contentType(MediaType.APPLICATION_JSON).content(appointment))
                .andExpect(status().isUnauthorized());
        mvc.perform(post("/appointments/" + token).contentType(MediaType.APPLICATION_JSON).content(appointment))
                .andExpect(status().isCreated());
        mvc.perform(post("/appointments/" + token).contentType(MediaType.APPLICATION_JSON).content(appointment))
                .andExpect(status().isConflict());
        mvc.perform(get("/patient/" + patient.getId() + "/patient/" + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.appointments[0].patientName").value("Test Patient"));
        mvc.perform(get("/appointments/" + date + "/null/" + doctorToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.appointments[0].doctorName").value("Appointment Doctor"));
        mvc.perform(get("/patient/" + patient.getId() + "/patient/" + doctorToken))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/patient/" + (patient.getId() + 9999) + "/patient/" + token))
                .andExpect(status().isForbidden());
        Long appointmentId = appointments.findByPatientId(patient.getId()).stream()
                .filter(a -> a.getDoctor().getId().equals(doctor.getId())).findFirst().orElseThrow().getId();
        mvc.perform(get("/prescription/" + appointmentId + "/" + token))
                .andExpect(status().isUnauthorized());
        mvc.perform(get("/prescription/" + appointmentId + "/" + doctorToken))
                .andExpect(status().isOk()).andExpect(jsonPath("$.prescription").isArray());
        mvc.perform(delete("/appointments/" + appointmentId + "/" + token))
                .andExpect(status().isOk());
        mvc.perform(get("/patient/filter/null/null/" + token))
                .andExpect(status().isOk()).andExpect(jsonPath("$.appointments[0].status").value(2));
    }
}
