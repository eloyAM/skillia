package com.example.application.utils;

import com.example.application.dto.PersonDto;
import jakarta.annotation.Nullable;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
public final class Comparators {

    /**
     * Returns true if some of the person attributes contains the filter value, case-insensitive.
     * Attributes: display name, username, email, title, department
     */
    public static boolean personDtoAttributesContains(
        @Nullable PersonDto person,
        @Nullable String filterValue
    ) {
        if (person == null) {
            return false;
        }
        return StringUtils.containsIgnoreCase(person.getFullName(), filterValue)
            || StringUtils.containsIgnoreCase(person.getUsername(), filterValue)
            || StringUtils.containsIgnoreCase(person.getTitle(), filterValue)
            || StringUtils.containsIgnoreCase(person.getDepartment(), filterValue)
            || StringUtils.containsIgnoreCase(person.getEmail(), filterValue);
    }
}
