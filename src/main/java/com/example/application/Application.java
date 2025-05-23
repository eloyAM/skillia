package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * The entry point of the Spring Boot application.
 * Use the @PWA annotation to make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@ConfigurationPropertiesScan({
    "com.example.application.security",
    "com.example.application.ldap"
})
@EnableWebMvc
@Theme("skillia")
@PWA(
        name = "Skillia",
        shortName = "Skillia",
        offlinePath = "offline.html",
        offlineResources = {"images/offline.png"}
)
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}
