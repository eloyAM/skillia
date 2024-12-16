package com.example.application.entity;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;
import org.hibernate.proxy.HibernateProxy;

import java.util.Objects;

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

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) return false;
        SkillTag skillTag = (SkillTag) o;
        return getId() != null && Objects.equals(getId(), skillTag.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}