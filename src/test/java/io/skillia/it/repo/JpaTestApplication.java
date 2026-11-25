package io.skillia.it.repo;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootConfiguration
@EnableAutoConfiguration
@EntityScan(basePackages = "io.skillia.persistence.entity")
@EnableJpaRepositories(basePackages = "io.skillia.persistence.repo")
class JpaTestApplication {
    // no @ComponentScan on purpose
}
