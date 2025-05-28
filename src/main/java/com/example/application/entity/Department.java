package com.example.application.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "department")
public class Department {
    // Column "name"
    public static final String COLUMN_NAME = "name";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "department__id_gen")
    @SequenceGenerator(name = "department__id_gen", sequenceName = "department__id_seq", initialValue = 50, allocationSize = 1)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = COLUMN_NAME, nullable = false, unique = true, updatable = false, length = 50)
    private String name;

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "department_skill_groups",
        joinColumns = @JoinColumn(name = "department_id", foreignKey = @ForeignKey(name = "FK__department_skill_groups__department_id")),
        inverseJoinColumns = @JoinColumn(name = "group_id", foreignKey = @ForeignKey(name = "FK__department_skill_groups__group_id"))
    )
    private List<SkillGroup> skillGroups = new ArrayList<>();

    @OneToMany(orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = Person.COLUMN_DEPARMENT, referencedColumnName = COLUMN_NAME)
    private List<Person> people = new ArrayList<>();

}