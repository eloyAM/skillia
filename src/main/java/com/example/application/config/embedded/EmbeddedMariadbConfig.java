package com.example.application.config.embedded;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

@Configuration
@Profile("embedded-mariadb")
public class EmbeddedMariadbConfig {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddedMariadbConfig.class);
    public static final String DB_NAME = "test";
    public static final String DB_USER = "root";
    public static final String DB_PASSWORD = "";
    public static final String DRIVER_CLASS_NAME = "org.mariadb.jdbc.Driver";
    private DB mariadb;

    @Bean
    public DataSource dataSource() throws ManagedProcessException {
        logger.info("Starting an embedded maria db");
        mariadb = DB.newEmbeddedDB(
                DBConfigurationBuilder.newBuilder().build()
        );
        mariadb.start();
        createDB(DB_NAME, DB_USER, DB_PASSWORD);
        String jdbcUrl = mariadb.getConfiguration().getURL(DB_NAME);
        logger.info("Initializing the datasource with an embedded maria db with the following url: {}", jdbcUrl);
        return DataSourceBuilder.create()
                .url(jdbcUrl)
                .username(DB_USER)
                .password(DB_PASSWORD)
                .driverClassName(DRIVER_CLASS_NAME)
                .build();
    }

    @PreDestroy
    public void close() throws ManagedProcessException {
        logger.info("Closing the embedded maria db");
        mariadb.stop();
        logger.info("The embedded maria db has been closed");
    }

    void createDB(String dbName, String username, String password) throws ManagedProcessException {
        String command = "create database " + dbName + " default character set utf8 collate utf8_general_ci;";
        mariadb.run(command, username, password, null); // mariadb.createDB(dbName) but with custom charset
    }
}