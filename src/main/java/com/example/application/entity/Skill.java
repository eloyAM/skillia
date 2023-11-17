package com.example.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.Collection;
import java.util.Objects;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@Entity
@Table(name = "skill")
public class Skill {
    // DB ID problem -> Nullable when saving, NonNull when saved :)
    // Using @NonNull gets very tricky -> get rid of it
    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @NonNull
    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;    // TODO use as PK?

    @OneToMany(mappedBy = "skill")
    private Collection<PersonSkill> personSkills;

    public Skill(@NonNull Long id) {
        this.id = Objects.requireNonNull(id);
    }

    public Skill(Long id, @NonNull String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
    }
}