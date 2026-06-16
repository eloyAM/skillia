package io.skillia.dto.stats;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatValue {
    @Min(0)
    private Long count;
    @Min(0)
    private int min;
    @Max(5)
    private int max;
    @Min(0)
    @Max(5)
    private double average;
}
