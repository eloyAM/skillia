package io.skillia.ut;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import com.tngtech.archunit.lang.syntax.elements.ClassesShouldConjunction;
import com.tngtech.archunit.library.Architectures;
import com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition;
import io.skillia.support.bootstrap.ImportLdapUsersToDbAppRunner;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.net.URL;
import java.util.stream.Stream;

import static com.tngtech.archunit.library.plantuml.rules.PlantUmlArchCondition.adhereToPlantUmlDiagram;

class ArchUnitTest {
    private static final JavaClasses APPLICATION_CLASSES = new ClassFileImporter()
        .importPackages("io.skillia");

    private static final Architectures.LayeredArchitecture LAYERED_ARCHITECTURE = Architectures
        .layeredArchitecture()
        .consideringOnlyDependenciesInAnyPackage("io.skillia..")
        .layer("Security").definedBy("io.skillia.security..")
        .layer("View").definedBy("io.skillia.view..")
        .layer("RestAPI").definedBy("io.skillia.restcontroller..")
        .layer("Bootstrap").definedBy("io.skillia.support.bootstrap")
        .layer("Ldap").definedBy("io.skillia.ldap..", "io.skillia.service.ldap")
        .layer("Service").definedBy("io.skillia.service", "io.skillia.service.utils")
        .layer("Repo").definedBy("io.skillia.persistence.repo..")
        .layer("Mapper").definedBy("io.skillia.mapper..")
        .layer("Dto").definedBy("io.skillia.dto..")
        .layer("Entity").definedBy("io.skillia.persistence.entity..");

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
            .consideringOnlyDependenciesInAnyPackage("io.skillia..")
            // Architecture definition
            .layer("Model").definedBy("io.skillia.dto..")
            .layer("Presentation").definedBy(
                "io.skillia.view..",
                "io.skillia.restcontroller.."
            )
            .layer("Business").definedBy(
                "io.skillia.service..",
                "io.skillia.security..",
                "io.skillia.mapper..",
                "io.skillia.support.bootstrap"
            )
            .layer("DataAccess").definedBy(
                "io.skillia.ldap..",
                "io.skillia.persistence.repo..",
                "io.skillia.persistence.entity.."
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
            "io.skillia.dto..", "io.skillia.mapper..", "io.skillia.persistence.entity.."
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
                "io.skillia.dto..",
                "io.skillia.service..",
                "io.skillia.security",
                "io.skillia.security.view"
            );
            String ownPackage = "io.skillia.view..";
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
            "io.skillia.restcontroller",
            "io.skillia.restcontroller.config",
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
                "io.skillia.dto..",
                "io.skillia.service..",
                "io.skillia.security"
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
            String ownPackage = "io.skillia.dto..";
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
            String ownPackage = "io.skillia.persistence.entity..";
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
                "io.skillia.dto..",
                "io.skillia.persistence.entity.."
            );
            String ownPackage = "io.skillia.mapper..";
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
            "io.skillia.dto..",
            "io.skillia.mapper..",
            "io.skillia.persistence.repo..",
            "io.skillia.security",
            "io.skillia.ldap.client"
        );
        String ownPackage = "io.skillia.service..";
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
            "io.skillia.dto..",
            "io.skillia.persistence.entity.."
        );
        String ownPackage = "io.skillia.persistence.repo..";
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
            "io.skillia.view.page.login",
            "io.skillia.dto..",
            "io.skillia.service..",
            "io.skillia.ldap.properties.."
        );
        String ownPackage = "io.skillia.security..";
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
            "io.skillia.dto..",
            "io.skillia.service.."
        );
        String ownPackage = "io.skillia.ldap..";
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
            "io.skillia.service.ldap"
        );
        ArchRule rule = ArchRuleDefinition.theClass(ImportLdapUsersToDbAppRunner.class)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(APPLICATION_CLASSES);
    }
}
