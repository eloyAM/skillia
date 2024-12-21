package com.example.application.config.embedded;

import io.zonky.test.db.postgres.embedded.EmbeddedPostgres;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@Profile("embedded-postgres")
public class EmbeddedPostgresConfig {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedPostgresConfig.class);
    private EmbeddedPostgres postgres;

    @Bean
    public DataSource dataSource() throws IOException {
        logger.info("Starting an embedded postgres db");
        postgres = EmbeddedPostgres.start();
        DataSource dataSource = postgres.getPostgresDatabase();
        logger.info("Started an embedded postgres db with the following datasource: {}", dataSource);
        return dataSource;
    }

    @PreDestroy
    public void close() throws IOException {
        logger.info("Closing the embedded postgres db");
        postgres.close();
        logger.info("The embedded postgres db has been closed");
    }
}