package io.skillia.dto.skillperson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.skillia.dto.main.SkillDto;
import io.skillia.persistence.entity.PersonSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link PersonSkill}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillAndPeopleWithLevel implements Serializable {
    @NonNull
    private SkillDto skill;
    private List<PersonWithLevelDto> peopleWithLevel;
}
