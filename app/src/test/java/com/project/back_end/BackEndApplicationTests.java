package com.project.back_end;

import com.project.testsupport.PrescriptionTestConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(PrescriptionTestConfiguration.class)
class BackEndApplicationTests {

	@Test
	void contextLoads() {
	}

}
