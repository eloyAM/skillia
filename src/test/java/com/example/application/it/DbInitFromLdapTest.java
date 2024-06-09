package com.example.application.it;

import com.example.application.dto.PersonDto;
import com.example.application.service.PersonService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
public class DbInitFromLdapTest {
    private static final int LDAP_PORT = 1389;
    @Autowired
    private PersonService personService;
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.1-alpine3.18");
    @Container
    private static final GenericContainer<?> openLdapContainer = new GenericContainer<>("bitnami/openldap:2.6.6-debian-11-r59")
            .withExposedPorts(LDAP_PORT)
            .withCopyFileToContainer(MountableFile.forHostPath("./src/test/resources/ldif/test-01.ldif"), "/ldifs/test-01.ldif")
            .withEnv("LDAP_ADMIN_USERNAME", "admin")
            .withEnv("LDAP_ADMIN_PASSWORD", "adminpassword")
            .withEnv("LDAP_ROOT", "dc=example,dc=org")
            .withEnv("LDAP_ADMIN_DN", "cn=admin,dc=example,dc=org");

    @DynamicPropertySource
    private static void initProperties(DynamicPropertyRegistry registry) {
        registry.add("ldap.url", () -> "ldap://localhost:" + openLdapContainer.getMappedPort(LDAP_PORT));
    }

    @Test
    void dbIsInitializedFromLdap() {
        assertThat(personService.findAllPerson())
                .containsExactlyInAnyOrderElementsOf(getExpectedPersonList());
    }

    List<PersonDto> getExpectedPersonList() {
        return List.of(
                PersonDto.builder()
                        .username("hugo.reyes")
                        .fullName("Hugo Reyes Herrero")
                        .email("hugo.reyes@example.org")
                        .title("HR Manager")
                        .department("Human Resources")
                        .build(),
                PersonDto.builder()
                        .username("andrea.riquelme")
                        .fullName("Andrea Riquelme Ruiz")
                        .email("andrea.riquelme@example.org")
                        .title("Software Engineer")
                        .department("Innovation")
                        .build()
        );
    }
}
