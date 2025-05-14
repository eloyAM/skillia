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

    @Column(name = "department", length = 50)
    private String department;

    @OneToMany(mappedBy = "person")
    private Collection<PersonSkill> personSkills;
}