package com.example.application;

import com.vaadin.flow.component.page.AppShellConfigurator;
import com.vaadin.flow.server.PWA;
import com.vaadin.flow.theme.Theme;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Arrays;

/**
 * The entry point of the Spring Boot application.
 * Use the @PWA annotation to make the application installable on phones, tablets
 * and some desktop browsers.
 */
@SpringBootApplication
@Theme("skillia")
@PWA(
        name = "Skillia",
        shortName = "Skillia",
        offlinePath = "offline.html",
        offlineResources = {"images/offline.png"}
)
public class Application implements AppShellConfigurator {

    public static void main(String[] args) {
        ConfigurableApplicationContext applicationContext = SpringApplication.run(Application.class, args);
//        displayAllBeans(applicationContext);
    }

    private static void displayAllBeans(ApplicationContext applicationContext) {
        String[] allBeanNames = applicationContext.getBeanDefinitionNames();
        System.out.println(Arrays.toString(allBeanNames));
    }
}
