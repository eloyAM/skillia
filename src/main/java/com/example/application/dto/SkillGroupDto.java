package com.example.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.example.application.entity.SkillGroup}
 */
@Data
public class SkillGroupDto implements Serializable {
    Long id;
    @NotBlank
    String name;
    String description;
    List<SkillDto> skills;
}