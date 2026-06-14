package com.example.application.ut;

import com.example.application.bootstrap.ImportLdapUsersToDbAppRunner;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.syntax.ArchRuleDefinition;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.stream.Stream;


class ArchUnitTest {
    private final JavaClasses applicationClasses = new ClassFileImporter()
        .importPackages("com.example.application");

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
                "org.springframework.security..",
                "org.springframework.web..",
                // application
                "com.example.application.dto..",
                "com.example.application.service..",
                "com.example.application.security"
            );
            String ownPackage = "com.example.application.view..";
            Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
            ArchRule rule = ArchRuleDefinition.classes().that()
                .resideInAPackage(ownPackage)
                .should().onlyDependOnClassesThat()
                .resideInAnyPackage(allowedPackages.toArray(String[]::new));
            rule.check(applicationClasses);
        }
    }

    @Nested
    class RestController {
        @ParameterizedTest
        @ValueSource(strings = {
            "com.example.application.controller",
            "com.example.application.controller.config",
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
            rule.check(applicationClasses);
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
            rule.check(applicationClasses);
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
            rule.check(applicationClasses);
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
            rule.check(applicationClasses);
        }
    }

    @Test
    void servicePackageShouldOnlyDependOnClassesThatResideInGivenPackages() {
        var initialPackages = Stream.of(
            "java..",
            "jakarta..",
            // spring
            "org.springframework.security..",
            "org.springframework.stereotype..",
            "org.springframework.web..",
            // application
            "com.example.application.dto..",
            "com.example.application.utils..",
            "com.example.application.mapper..",
            "com.example.application.repo..",
            "com.example.application.security"
        );
        String ownPackage = "com.example.application.service..";
        Stream<String> allowedPackages = Stream.concat(initialPackages, Stream.of(ownPackage));
        ArchRule rule = ArchRuleDefinition.classes().that()
            .resideInAPackage(ownPackage)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(applicationClasses);
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
        rule.check(applicationClasses);
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
            "com.vaadin.flow.spring.security..",
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
        rule.check(applicationClasses);
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
        rule.check(applicationClasses);
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
            "com.example.application.ldap"
        );
        ArchRule rule = ArchRuleDefinition.theClass(ImportLdapUsersToDbAppRunner.class)
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage(allowedPackages.toArray(String[]::new));
        rule.check(applicationClasses);
    }
}
