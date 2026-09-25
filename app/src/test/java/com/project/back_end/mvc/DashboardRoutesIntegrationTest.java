package com.project.back_end.mvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.project.back_end.models.Admin;
import com.project.back_end.models.Doctor;
import com.project.back_end.repo.AdminRepository;
import com.project.back_end.repo.DoctorRepository;
import com.project.back_end.services.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardRoutesIntegrationTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private AdminRepository admins;

    @Autowired
    private DoctorRepository doctors;

    @Autowired
    private TokenService tokens;

    @Test
    @Transactional
    void rendersThymeleafOnlyForTheMatchingRole() throws Exception {
        Admin admin = new Admin();
        admin.setUsername("dashboard-test-admin");
        admin.setPassword("disabled");
        admins.save(admin);

        Doctor doctor = new Doctor();
        doctor.setName("Test Doctor");
        doctor.setSpecialty("Cardiology");
        doctor.setEmail("dashboard-test-doctor@clinic.example");
        doctor.setPassword("disabled");
        doctor.setPhone("1234567890");
        doctors.save(doctor);

        String adminToken = tokens.generateToken(admin.getUsername(), "admin");
        String doctorToken = tokens.generateToken(doctor.getEmail(), "doctor");
        mvc.perform(get("/adminDashboard/{token}", adminToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Manage Doctors")));
        mvc.perform(get("/doctorDashboard/{token}", doctorToken))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Patient Appointments")));
        mvc.perform(get("/doctorDashboard/{token}", adminToken))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(get("/adminDashboard/invalid"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }
}
