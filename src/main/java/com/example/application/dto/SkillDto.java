package com.example.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * DTO for {@link com.example.application.entity.Skill}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@RequiredArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class SkillDto implements Serializable {
    private Long id;
    @NonNull
    @NotBlank
    private String name;
    @NotNull
    @Builder.Default
    private Set<SkillTagDto> tags = new LinkedHashSet<>();

    public SkillDto(@NonNull Long skillId, @NonNull @NotBlank String skillName) {
        this(skillId, skillName, new LinkedHashSet<>());
    }
}