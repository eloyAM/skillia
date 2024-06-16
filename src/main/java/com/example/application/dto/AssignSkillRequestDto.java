package com.example.application.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.checkerframework.checker.nullness.qual.NonNull;

/**
 * DTO for {@link com.example.application.entity.PersonSkill}.
 */
public record AssignSkillRequestDto(
        @NonNull @NotNull @Min(1) @Max(5) Integer level
) {
}
