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
    @Id
    @Column(name = "username", nullable = false)
    @jakarta.validation.constraints.NotNull
    // Lombok supported annotation to detect required fields
    @org.checkerframework.checker.nullness.qual.NonNull
    @NotBlank
    private String username;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email")
    private String email;

    @Column(name = "title")
    private String title;

    @Column(name = "department")
    private String department;

    @OneToMany(mappedBy = "person")
    private Collection<PersonSkill> personSkills;
}