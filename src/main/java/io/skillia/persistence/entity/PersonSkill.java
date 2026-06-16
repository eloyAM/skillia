package io.skillia.persistence.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder(toBuilder = true)
@Entity
@Table(name = "person_skill")
@EntityListeners(AuditingEntityListener.class)
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
            level,
            null
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
    @Min(1)
    @Max(5)
    @NotNull
    @Column(name = "level", nullable = false)
    private Integer level;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private Instant lastModifiedDate;
}