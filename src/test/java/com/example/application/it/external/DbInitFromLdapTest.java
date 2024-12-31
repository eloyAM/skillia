package com.example.application.it.external;

import com.example.application.dto.PersonDto;
import com.example.application.service.PersonService;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.MountableFile;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@EnabledIf("isDockerAvailable")
@ActiveProfiles("default")   // Be careful to don't runt it with the embedded postgres or LDAP to avoid conflicts
@SpringBootTest
@Testcontainers
public class DbInitFromLdapTest {
    private static final int LDAP_PORT = 1389;
    private static final String LDAP_BASE = "dc=example,dc=org";
    public static final String LDAP_ADMIN_SIMPLE_USERNAME = "admin-testcontainers";
    private static final String LDAP_ADMIN_DN = "cn=" + LDAP_ADMIN_SIMPLE_USERNAME + ",dc=example,dc=org";
    private static final String LDAP_ADMIN_PWD = "adminpassword";

    @Autowired
    private PersonService personService;
    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:16.1-alpine3.18");
    @Container
    private static final GenericContainer<?> openLdapContainer = new GenericContainer<>("bitnami/openldap:2.6.6-debian-11-r59")
            .withExposedPorts(LDAP_PORT)
            .withCopyFileToContainer(MountableFile.forHostPath("./src/test/resources/ldif/test-01.ldif"), "/ldifs/test-01.ldif")
            .withEnv("LDAP_ADMIN_USERNAME", LDAP_ADMIN_SIMPLE_USERNAME)
            .withEnv("LDAP_ADMIN_PASSWORD", LDAP_ADMIN_PWD)
            .withEnv("LDAP_ROOT", LDAP_BASE)
            .withEnv("LDAP_ADMIN_DN", LDAP_ADMIN_DN);

    @DynamicPropertySource
    private static void initProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.ldap.urls", () -> "ldap://localhost:" + openLdapContainer.getMappedPort(LDAP_PORT));
        registry.add("spring.ldap.base", () -> LDAP_BASE);
        registry.add("spring.ldap.username", () -> LDAP_ADMIN_DN);
        registry.add("spring.ldap.password", () -> LDAP_ADMIN_PWD);
    }

    static boolean isDockerAvailable() {
        log.info("Checking if docker available");
        boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
        log.info("Docker available? : {}", dockerAvailable);
        return dockerAvailable;
    }

    @Test
    void dbIsInitializedFromLdap() {
        final List<PersonDto> expectedPersonList = getExpectedPersonList();
        assertThat(personService.findAllPerson())
                .containsAll(expectedPersonList)
                .containsExactlyInAnyOrderElementsOf(expectedPersonList);
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
