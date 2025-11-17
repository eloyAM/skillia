package com.example.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Collection;

@Getter
@Setter
@NoArgsConstructor(force = true)
@RequiredArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
@Entity
@Table(name = "person")
public class Person {
    public static final String COLUMN_DEPARTMENT = "department";

    @Id
    @Column(name = "username", nullable = false, length = 40)
    @jakarta.validation.constraints.NotNull
    // Lombok supported annotation to detect required fields
    @org.checkerframework.checker.nullness.qual.NonNull
    @NotBlank
    private String username;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(name = "email", length = 254)
    private String email;

    @Column(name = "title", length = 50)
    private String title;

    // Hint: without the "updatable = false", H2 creates another column with the same name for
    // the @JoinColumn (look at the Department entity) instead of using this one
    // Not a problem with a custom initialization of the schema (SQl script/flyway/liquibase)
    @Column(name = COLUMN_DEPARTMENT, length = 50, updatable = false)
    private String department;

    @OneToMany(mappedBy = "person")
    private Collection<PersonSkill> personSkills;
}