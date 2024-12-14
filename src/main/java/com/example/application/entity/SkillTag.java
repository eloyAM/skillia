package com.example.application.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@Entity
@Table(name = "skill_tag")
public class SkillTag {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skill_tag_id_gen")
    @SequenceGenerator(name = "skill_tag_id_gen", sequenceName = "skill_tag_id_seq", allocationSize = 1, initialValue = 50)
    @Column(name = "id", nullable = false)
    private Long id;

    @Nonnull
    @Column(name = "name", unique = true, nullable = false)
    private String name;
}