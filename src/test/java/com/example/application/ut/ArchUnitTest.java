package com.example.application.ut;

import com.example.application.bootstrap.ImportLdapUsersToDbAppRunner;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;
import java.util.stream.Stream;

import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;

class ArchUnitTest {
    private static final JavaClasses APPLICATION_CLASSES = new ClassFileImporter()
        .importPackages("com.example.application");

    private static final Architectures.LayeredArchitecture LAYERED_ARCHITECTURE = Architectures
        .layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("com.example.application..")
        .layer("Security").definedBy("com.example.application.security..")
        .layer("View").definedBy("com.example.application.view..")
        .layer("RestAPI").definedBy("com.example.application.restcontroller..")
        .layer("Bootstrap").definedBy("com.example.application.bootstrap")
        .layer("Ldap").definedBy("com.example.application.ldap..", "com.example.application.service.ldap")
        .layer("Service").definedBy("com.example.application.service", "com.example.application.service.utils")
        .layer("Repo").definedBy("com.example.application.repo..")
        .layer("Mapper").definedBy("com.example.application.mapper..")
        .layer("Dto").definedBy("com.example.application.dto..")
        .layer("Entity").definedBy("com.example.application.entity..");

    @Test
    void layeredArchitectureShouldBeRespected() {
        ArchRule rule = LAYERED_ARCHITECTURE
            // Allowed dependencies
            .whereLayer("Security").mayOnlyAccessLayers("Dto", "View", "Ldap")
            .whereLayer("View").mayOnlyAccessLayers("Service", "Dto", "Security")
            .whereLayer("RestAPI").mayOnlyAccessLayers("Service", "Dto", "Security")
            .whereLayer("Bootstrap").mayOnlyAccessLayers("Ldap")
            .whereLayer("Ldap").mayOnlyAccessLayers("Dto", "Service")
            .whereLayer("Service").mayOnlyAccessLayers("Repo", "Mapper", "Dto", "Security")
            .whereLayer("Repo").mayOnlyAccessLayers("Entity", "Dto")
            .whereLayer("Mapper").mayOnlyAccessLayers("Dto", "Entity")
            .whereLayer("Dto").mayNotAccessAnyLayer()
            .whereLayer("Entity").mayNotAccessAnyLayer();
        rule.check(APPLICATION_CLASSES);
    }

    @Test
    void layeredArchitectureSimplifiedShouldBeRespected() {
        Architectures.LayeredArchitecture layeredArchitecture = Architectures
            .layeredArchitecture()
            .consideringOnlyDependenciesInAnyPackage("com.example.application..")
            // Architecture definition
            .layer("Model").definedBy("com.example.application.dto..")
            .layer("Presentation").definedBy(
                "com.example.application.view..",
                "com.example.application.restcontroller.."
            )
            .layer("Business").definedBy(
                "com.example.application.service..",
                "com.example.application.security..",
                "com.example.application.mapper..",
                "com.example.application.bootstrap"
            )
            .layer("DataAccess").definedBy(
                "com.example.application.ldap..",
                "com.example.application.repo..",
                "com.example.application.entity.."
            );
        // Architecture rules
        ArchRule rule = layeredArchitecture
            .whereLayer("Model").mayNotAccessAnyLayer()
            .whereLayer("Presentation").mayOnlyAccessLayers("Business", "Model")
            .whereLayer("Business").mayOnlyAccessLayers("DataAccess", "Model")
            .whereLayer("DataAccess").mayOnlyAccessLayers("Model");
        rule.check(APPLICATION_CLASSES);
    }

    @Test
    void plantUmlExample() {
        URL diagram = getClass().getResource("/archunit/app-components-example.puml");

        ClassesShouldConjunction rule = ArchRuleDefinition.classes().should(adhereToPlantUmlDiagram(
                diagram,
                PlantUmlArchCondition.Configuration.consideringOnlyDependenciesInDiagram()
            )
        );
        rule.check(new ClassFileImporter().importPackages(
            "com.example.application.dto..", "com.example.application.mapper..", "com.example.application.entity.."
        ));
    }

    @Nested
    class View {
        @Test
        void viewPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
            var initialPackages = Stream.of(
                "java..",
                "jakarta..",
                "elemental.json..",
                "org.apache.commons.lang3..",
                // vaadin
                "com.vaadin.flow..",
                "com.vaadin.componentfactory..",
                // spring
                "org.springframework.beans.factory.annotation",
                "org.springframework.context.annotation",
                "org.springframework.security..",
                "org.springframework.web..",
                // application
                "com.example.application.dto..",
                "com.example.application.service..",
                "com.example.application.security",
                "com.example.application.security.view"
            );
            String ownPackage = "com.example.application.view..";
            Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
            ArchRule rule = ArchRuleDefinition.classes().that()
                .resideInAPackage(ownPackage)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages.toArray(String[]::new));
            rule.check(APPLICATION_CLASSES);
        }
    }

    @Nested
    class RestController {
        @ParameterizedTest
        @ValueSource(strings = {
            "com.example.application.restcontroller",
            "com.example.application.restcontroller.config",
        })
        void restControllerPackagesShouldOnlyDependOnClassesThatResideInGivenPackages(String ownPackage) {
            var initialPackages = Stream.of(
                "java..",
                "jakarta..",
                "io.swagger..",
                // spring
                "org.springframework.http..",
                "org.springframework.security..",
                "org.springframework.validation..",
                "org.springframework.web..",
                // application
                "com.example.application.dto..",
                "com.example.application.service..",
                "com.example.application.security"
            );
            Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
            ArchRule rule = ArchRuleDefinition.classes().that()
                .resideInAPackage(ownPackage)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages.toArray(String[]::new));
            rule.check(APPLICATION_CLASSES);
        }
    }

    @Nested
    class Model {
        @Test
        void dtoPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
            var initialPackages = Stream.of(
                "java..",
                "jakarta.validation.constraints..",
                "com.fasterxml.jackson.annotation.."
                // no spring dependencies
                // no application dependencies
            );
            String ownPackage = "com.example.application.dto..";
            Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
            ArchRule rule = ArchRuleDefinition.classes().that()
                .resideInAPackage(ownPackage)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages.toArray(String[]::new));
            rule.check(APPLICATION_CLASSES);
        }

        @Test
        void entityPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
            var initialPackages = Stream.of(
                "java..",
                // jakarta
                "jakarta.validation.constraints..",
                "jakarta.annotation..",
                "jakarta.persistence..",
                // hibernate
                "org.hibernate.annotations..",
                "org.hibernate.proxy..",
                // spring dependencies
                "org.springframework.data.jpa.domain.support..",
                "org.springframework.data.annotation.."
                // no application dependencies
            );
            String ownPackage = "com.example.application.entity..";
            Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
            ArchRule rule = ArchRuleDefinition.classes().that()
                .resideInAPackage(ownPackage)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages.toArray(String[]::new));
            rule.check(APPLICATION_CLASSES);
        }

        @Test
        void mapperPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
            var initialPackages = Stream.of(
                "java..",
                "org.mapstruct..",
                "org.springframework.stereotype..",
                // application
                "com.example.application.dto..",
                "com.example.application.entity.."
            );
            String ownPackage = "com.example.application.mapper..";
            Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
            ArchRule rule = ArchRuleDefinition.classes().that()
                .resideInAPackage(ownPackage)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages.toArray(String[]::new));
            rule.check(APPLICATION_CLASSES);
        }
    }

    @Test
    void servicePackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
        var initialPackages = Stream.of(
            "java..",
            "jakarta..",
            "org.slf4j..",
            "org.apache.commons.lang3..",
            // spring
            "org.springframework.security..",
            "org.springframework.stereotype..",
            "org.springframework.web..",
            // application
            "com.example.application.dto..",
            "com.example.application.mapper..",
            "com.example.application.repo..",
            "com.example.application.security",
            "com.example.application.ldap.client"
        );
        String ownPackage = "com.example.application.service..";
        Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
        ArchRule rule = ArchRuleDefinition.classes().that()
            .resideInAPackage(ownPackage)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(APPLICATION_CLASSES);
    }

    @Test
    void repoPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
        var initialPackages = Stream.of(
            "java..",
            // spring
            "org.springframework.stereotype..",
            "org.springframework.data.jpa.repository..",
            "org.springframework.transaction.annotation..",
            // application
            "com.example.application.dto..",
            "com.example.application.entity.."
        );
        String ownPackage = "com.example.application.repo..";
        Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
        ArchRule rule = ArchRuleDefinition.classes().that()
            .resideInAPackage(ownPackage)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(APPLICATION_CLASSES);
    }

    @Test
    void securityPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
        var initialPackages = Stream.of(
            "java..",
            "javax.crypto..",
            "jakarta.validation.constraints..",
            "jakarta.servlet.http..",
            "com.nimbusds.jose..",
            "com.nimbusds.jwt..",
            // vaadin
            "com.vaadin.flow.spring.security..",
            "com.vaadin.flow.component",    // Login component
            // spring
            "org.springframework.beans.factory.annotation",
            "org.springframework.core.env..",
            "org.springframework.context.annotation..",
            "org.springframework.http..",
            "org.springframework.validation.annotation..",
            "org.springframework.stereotype..",
            "org.springframework.boot.autoconfigure.security.servlet..",
            "org.springframework.boot.context.properties..",
            "org.springframework.ldap.core.support..",
            "org.springframework.security..",
            // application
            "com.example.application.view.page.login",
            "com.example.application.dto..",
            "com.example.application.service..",
            "com.example.application.ldap.properties.."
        );
        String ownPackage = "com.example.application.security..";
        Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
        ArchRule rule = ArchRuleDefinition.classes().that()
            .resideInAPackage(ownPackage)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(APPLICATION_CLASSES);
    }

    @Test
    void ldapPackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
        var initialPackages = Stream.of(
            "java..",
            "javax.naming..",
            "jakarta.validation.constraints..",
            "org.slf4j..",
            "org.apache.commons.lang3..",
            // spring
            "org.springframework.beans.factory.annotation",
            "org.springframework.boot.context.properties..",
            "org.springframework.ldap.core..",
            "org.springframework.stereotype..",
            "org.springframework.validation.annotation..",
            // application
            "com.example.application.dto..",
            "com.example.application.service.."
        );
        String ownPackage = "com.example.application.ldap..";
        Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
        ArchRule rule = ArchRuleDefinition.classes().that()
            .resideInAPackage(ownPackage)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(APPLICATION_CLASSES);
    }

    @Test
    void importFromLdapRunnerShouldOnlyDependOnClassesThatResideInGivenPackages() {
        var allowedPackages = Stream.of(
            "java..",
            "org.slf4j..",
            // spring
            "org.springframework.boot",
            "org.springframework.core.annotation",
            "org.springframework.stereotype",
            // application
            "com.example.application.service.ldap"
        );
        ArchRule rule = ArchRuleDefinition.theClass(ImportLdapUsersToDbAppRunner.class)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(APPLICATION_CLASSES);
    }
}
