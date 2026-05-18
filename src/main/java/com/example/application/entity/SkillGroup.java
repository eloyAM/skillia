package com.example.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "skill_group")
public class SkillGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skill_group__id_gen")
    @SequenceGenerator(name = "skill_group__id_gen", sequenceName = "skill_group__id_seq", allocationSize = 1, initialValue = 50)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", length = 250)
    private String description;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "skill_group_skills",
        joinColumns = @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "FK__skill_group_skills__group_id")),
        inverseJoinColumns = @JoinColumn(name = "skill_id", foreignKey = @ForeignKey(name = "FK__skill_group_skills__skill_id")),
        // Set type -> both columns are used as primary key -> no need to add manually a unique constraint
        indexes = @Index(name = "IDX__skill_group_skills__group_id", columnList = "group_id")
    )
    private Set<Skill> skills = new HashSet<>();

}