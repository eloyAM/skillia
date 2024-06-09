package com.example.application.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.validator.constraints.Range;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@Entity
@Table(name = "person_skill")
public class PersonSkill {

    public PersonSkill(@NonNull String personId, @NonNull Long skillId,
                       @NonNull @Min(1) @Max(5) Integer level) {
        this(new PersonSkillId(personId, skillId), level);
    }

    public PersonSkill(@NonNull PersonSkillId personSkillId,
                       @NonNull @Min(1) @Max(5) Integer level) {
        this(personSkillId,
            new Person(personSkillId.getPersonId()),
            new Skill(personSkillId.getSkillId()),
            level
        );
    }

    @NonNull
    @EmbeddedId
    private PersonSkillId personSkillId;

    @NonNull
    @ManyToOne
    @MapsId("personId")
    @JoinColumn(name = "person_id", foreignKey = @ForeignKey(name = "FK__person_skill__person"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Person person;

    @NonNull
    @ManyToOne
    @MapsId("skillId")
    @JoinColumn(name = "skill_id", foreignKey = @ForeignKey(name = "FK__person_skill__skill"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Skill skill;

    @NonNull
    @Range(min = 1, max = 5)
    @NotNull
    @Column(name = "level", nullable = false)
    private Integer level;
}