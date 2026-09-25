package com.project.back_end.mvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

import com.project.back_end.services.Service;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DashboardControllerTest {

    private final Service service = new Service(null) {
        @Override
        public Map<String, String> validateToken(String token, String role) {
            if (token.equals(role + "-token")) {
                return Map.of();
            }
            return Map.of("error", "Invalid token");
        }
    };
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        mvc = MockMvcBuilders.standaloneSetup(new DashboardController(service)).build();
    }

    @Test
    void rendersOnlyTheAuthorizedDashboard() throws Exception {
        mvc.perform(get("/adminDashboard/admin-token"))
                .andExpect(status().isOk()).andExpect(view().name("admin/adminDashboard"))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(header().string("Referrer-Policy", "no-referrer"));
        mvc.perform(get("/doctorDashboard/doctor-token"))
                .andExpect(status().isOk()).andExpect(view().name("doctor/doctorDashboard"));
    }

    @Test
    void invalidOrWrongRoleTokensRedirectToHome() throws Exception {
        mvc.perform(get("/adminDashboard/invalid"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(get("/adminDashboard/doctor-token"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
        mvc.perform(get("/doctorDashboard/admin-token"))
                .andExpect(status().is3xxRedirection()).andExpect(redirectedUrl("/"));
    }
}
