package com.project.testsupport;

import com.project.back_end.repo.PrescriptionRepository;
import java.lang.reflect.Proxy;
import java.util.List;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@TestConfiguration
public class PrescriptionTestConfiguration {
    @Bean
    @Primary
    PrescriptionRepository testPrescriptions() {
        return (PrescriptionRepository) Proxy.newProxyInstance(
                PrescriptionRepository.class.getClassLoader(),
                new Class<?>[] {PrescriptionRepository.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "findByAppointmentId" -> List.of();
                    case "hashCode" -> System.identityHashCode(proxy);
                    case "equals" -> proxy == args[0];
                    case "toString" -> "TestPrescriptionRepository";
                    default -> throw new UnsupportedOperationException(
                            "Unexpected prescription repository call: " + method.getName());
                });
    }
}
