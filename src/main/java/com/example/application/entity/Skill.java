package com.example.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "skill_id_gen")
    @SequenceGenerator(name = "skill_id_gen", sequenceName = "skill_id_seq", allocationSize = 1, initialValue = 50)
    @Column(name = "id", nullable = false)
    private Long id;

    @NonNull
    @NotBlank
    @Column(name = "name", nullable = false, unique = true)
    private String name;    // TODO use as PK?

    @OneToMany(mappedBy = "skill")
    private Collection<PersonSkill> personSkills;

    // Delete rule -> deleting a skill or a tag should delete the related record of the join table
    @OnDelete(action = OnDeleteAction.CASCADE)
    @NotNull
    @ManyToMany
    @JoinTable(
            name = "skill_tagging",
            joinColumns = @JoinColumn(name = "skill_id", foreignKey = @ForeignKey(name = "FK__skill_tagging__skill")),
            inverseJoinColumns = @JoinColumn(name = "tag_id", foreignKey = @ForeignKey(name = "FK__skill_tagging__tag"))
    )
    @Builder.Default
    private Set<SkillTag> tags = new LinkedHashSet<>();

    public Skill(@NonNull Long id) {
        this.id = Objects.requireNonNull(id);
    }

    public Skill(Long id, @NonNull String name) {
        this.id = id;
        this.name = Objects.requireNonNull(name);
    }
}