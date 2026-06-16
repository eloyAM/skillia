package com.example.application.it.service;

import com.example.application.dto.main.SkillDto;
import com.example.application.dto.main.SkillTagDto;
import com.example.application.it.testutils.CleanDbExtension;
import com.example.application.service.SkillService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(CleanDbExtension.class)
@SpringBootTest
class SkillTaggingTest {
    @Autowired
    private SkillService skillService;
    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void assignTagToSkillOk() {
        SkillTagDto savedTag01 = skillService
            .saveSkillTag(SkillTagDto.builder().name("Tag A").build())
            .orElseThrow();
        skillService.saveSkillTag(new SkillTagDto().setName("tag b"));
        SkillDto skillDto = skillService.saveSkill(SkillDto.builder()
            .name("skill a")
            .tags(Set.of(savedTag01))
            .build()
        ).orElseThrow();

        assertAll(
            () -> assertThat(skillDto.getId()).isGreaterThan(0L),
            () -> assertThat(skillDto.getName()).isEqualTo("skill a"),
            () -> assertThat(skillDto.getTags()).isNotEmpty(),
            () -> assertThat(skillDto.getTags()).containsExactlyInAnyOrder(savedTag01)
        );
    }

    @Test
    void assignTagToSkillDeleteSkillOk() {
        SkillTagDto savedTag01 = skillService
            .saveSkillTag(SkillTagDto.builder().name("Tag A").build())
            .orElseThrow();
        SkillTagDto savedTag02 = skillService
            .saveSkillTag(new SkillTagDto().setName("tag b"))
            .orElseThrow();

        SkillDto skillDto = skillService.saveSkill(
            SkillDto.builder().name("skill a").tags(Set.of(savedTag01)).build()
        ).orElseThrow();
        assertThat(getSkillTaggingCount()).isEqualTo(1);

        skillService.deleteSkillById(skillDto.getId());
        assertThat(skillService.getAllSkill()).isEmpty();
        assertThat(getSkillTaggingCount()).isZero();

        assertThat(skillService.getAllSkillTag()).containsExactlyInAnyOrder(savedTag01, savedTag02);
    }

    @Test
    void assignTagToSkillDeleteTagOk() {
        // Arrange -> 2 tags, only one assigned to a skill
        SkillTagDto savedTag01 = skillService
            .saveSkillTag(SkillTagDto.builder().name("Tag A").build())
            .orElseThrow();
        SkillTagDto savedTag02 = skillService
            .saveSkillTag(new SkillTagDto().setName("tag b"))
            .orElseThrow();

        skillService.saveSkill(
            SkillDto.builder().name("skill a").tags(Set.of(savedTag01)).build()
        ).orElseThrow();
        assertThat(getSkillTaggingCount()).isEqualTo(1);

        // Act
        Long savedTag01Id = savedTag01.getId();
        skillService.deleteSkillTagById(savedTag01Id);

        // Assert
        assertThat(getSkillTaggingCount()).isZero();    // Relation automatically removed
        assertThat(skillService.getAllSkillTag()).containsExactlyInAnyOrder(savedTag02);    // Tag removed
    }

    // Helpers

    private int getSkillTaggingCount() {
        return JdbcTestUtils.countRowsInTable(jdbcClient, "skill_tagging");
    }
}