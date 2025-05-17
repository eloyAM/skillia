package com.example.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * DTO for {@link com.example.application.entity.Department}
 */
@Data
public class DepartmentDto implements Serializable {
    Long id;
    @NotBlank
    String name;
    List<SkillGroupDto> skillGroups;
}