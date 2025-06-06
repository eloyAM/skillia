package com.example.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Set;

/**
 * DTO for {@link com.example.application.entity.SkillGroup}
 */
@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class SkillGroupDto implements Serializable {
    private Long id;
    @NotBlank
    private String name;
    private String description;
    private Set<SkillDto> skills;
}