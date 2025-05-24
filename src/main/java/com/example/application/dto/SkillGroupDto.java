package com.example.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.example.application.entity.SkillGroup}
 */
@Data
@Builder
@NoArgsConstructor(force = true)
@AllArgsConstructor
public class SkillGroupDto implements Serializable {
    Long id;
    @NotBlank
    String name;
    String description;
    List<SkillDto> skills;
}