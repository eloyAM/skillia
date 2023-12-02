package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The entry point of the Spring Boot application.
 * Use the @PWA annotation to make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme(themeClass = Lumo.class, variant = Lumo.DARK)
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
