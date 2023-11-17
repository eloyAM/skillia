package com.example.application.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

@EqualsAndHashCode // required as a Composite-id
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Embeddable
public class PersonSkillId implements Serializable {
    @NonNull
    @NotBlank
    @Column(name = "person_id", nullable = false)
    private String personId;

    @NonNull
    @NotNull
    @Column(name = "skill_id", nullable = false)
    private Long skillId;   // TODO use generated id or given name as key?
}