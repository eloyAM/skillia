package com.example.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
@Builder
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonWithLevelDto implements Serializable {
    @NonNull
    private PersonDto person;
    @NonNull
    @Min(1)
    @Max(5)
    private Integer level;

    public PersonWithLevelDto(
        @NonNull @NotBlank String username, String fullName, String email, String title,
        String department,
        @Min(1) @Max(5) @NonNull Integer level
    ) {
        this.person = PersonDto.builder()
            .username(username)
            .fullName(fullName)
            .email(email)
            .title(title)
            .department(department).build();
        this.level = level;
    }
}