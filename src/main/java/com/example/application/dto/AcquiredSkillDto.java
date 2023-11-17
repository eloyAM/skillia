package com.example.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

/**
 * DTO for {@link com.example.application.entity.PersonSkill}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class AcquiredSkillDto implements Serializable {
    @NonNull
    private SkillDto skill;
    @NonNull
    @Min(1)
    @Max(5)
    private Integer level;

    // generate constructor with all skill fields
    public AcquiredSkillDto(
        @NonNull Long skillId,
        @NonNull String skillName,
        @Min(1) @Max(5) @NonNull Integer level
    ) {
        this.skill = new SkillDto(skillId, skillName);
        this.level = level;
    }
}