package com.example.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.Accessors;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.Serializable;

/**
 * DTO for {@link com.example.application.entity.Person}.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
@RequiredArgsConstructor
@Accessors(chain = true)
@Builder(toBuilder = true)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonDto implements Serializable {
    @NonNull
    @NotBlank
    private String username;
    private String fullName;
    @Email
    private String email;
    private String title;
    private String department;
}