package com.example.application.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SkillStatValue {
    private Long skillId;
    private StatValue statValue;
}
