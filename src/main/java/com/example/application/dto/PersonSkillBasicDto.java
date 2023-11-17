package com.example.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

/**
 * DTO for {@link com.example.application.entity.PersonSkillId}.
 */
@Data
@AllArgsConstructor
@Builder
@Accessors(chain = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonSkillBasicDto implements Serializable {
    @NonNull
    private String personId;
    @NonNull
    private Long skillId;
    @NonNull
    @Min(1)
    @Max(5)
    private Integer level;
}