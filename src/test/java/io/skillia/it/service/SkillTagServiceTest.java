package io.skillia.it.service;

import io.skillia.dto.main.SkillTagDto;
import io.skillia.service.SkillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest
class SkillTagServiceTest {
    @Autowired
    private SkillService skillService;

    @ParameterizedTest
    @ValueSource(strings = {"Diego", "Ramón", "Жанна", "«ταБЬℓσ»:", "1<2 & 4+1>3,", "now 20% off!", "٩(-̮̮̃-̃)۶", "ਈ",
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"
    })
    void saveSkillTag(String name) {
        SkillTagDto originalDto = new SkillTagDto(null, name);
        Optional<SkillTagDto> savedResultOpt = skillService.saveSkillTag(originalDto);
        SkillTagDto skillTagDto = savedResultOpt.orElseThrow();
        assertAll(
            () -> assertThat(skillTagDto.getName()).isEqualTo(name),
            () -> assertThat(skillTagDto.getId()).isGreaterThan(0L)
        );
    }

    @Test
    void getAllSkillTag() {
        skillService.saveSkillTag(SkillTagDto.builder().name("Tag A").build());
        skillService.saveSkillTag(new SkillTagDto().setName("tag b"));
        skillService.saveSkillTag(new SkillTagDto().setName("tAg  c"));
        List<SkillTagDto> savedSkillTagAll = skillService.getAllSkillTag();
        assertThat(savedSkillTagAll).hasSize(3);
        assertThat(savedSkillTagAll)
            .extracting(SkillTagDto::getName)
            .containsExactlyInAnyOrder(
                "Tag A",
                "tag b",
                "tAg  c"
            );
        assertThat(savedSkillTagAll)
            .allSatisfy(skillTagDto -> assertThat(skillTagDto.getId()).isGreaterThan(0L));
    }
}