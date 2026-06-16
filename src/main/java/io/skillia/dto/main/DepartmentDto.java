package io.skillia.dto.main;

import io.skillia.persistence.entity.Department;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for {@link Department}
 */
@Data
public class DepartmentDto implements Serializable {
    private Long id;
    @NotBlank
    private String name;
    private List<SkillGroupDto> skillGroups;
    private List<PersonDto> people = new ArrayList<>();
}