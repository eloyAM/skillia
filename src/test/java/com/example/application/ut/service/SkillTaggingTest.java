package com.example.application.ut.service;

import com.example.application.dto.SkillDto;
import com.example.application.dto.SkillTagDto;
import com.example.application.it.testutils.CleanDbExtension;
import com.example.application.service.SkillService;
import com.example.application.service.SkillTagService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.test.jdbc.JdbcTestUtils;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

@ExtendWith(CleanDbExtension.class)
@SpringBootTest
class SkillTaggingTest {
    @Autowired
    private SkillTagService skillTagService;
    @Autowired
    private SkillService skillService;
    @Autowired
    private JdbcClient jdbcClient;

    @Test
    void assignTagToSkillOk() {
        SkillTagDto savedTag01 = skillTagService.saveSkillTag(SkillTagDto.builder().name("Tag A").build()).orElseThrow();
        skillTagService.saveSkillTag(new SkillTagDto().setName("tag b"));
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
        SkillTagDto savedTag01 = skillTagService.saveSkillTag(SkillTagDto.builder().name("Tag A").build()).orElseThrow();
        SkillTagDto savedTag02 = skillTagService.saveSkillTag(new SkillTagDto().setName("tag b")).orElseThrow();

        SkillDto skillDto = skillService.saveSkill(
                SkillDto.builder().name("skill a").tags(Set.of(savedTag01)).build()
        ).orElseThrow();
        assertThat(getSkillTaggingCount()).isEqualTo(1);

        skillService.deleteSkillById(skillDto.getId());
        assertThat(skillService.getAllSkill()).isEmpty();
        assertThat(getSkillTaggingCount()).isZero();

        assertThat(skillTagService.getAllSkillTag()).containsExactlyInAnyOrder(savedTag01, savedTag02);
    }

    @Test
    void assignTagToSkillDeleteTagThrowsReferentialIntegrityConstraintViolation() {
        SkillTagDto savedTag01 = skillTagService.saveSkillTag(SkillTagDto.builder().name("Tag A").build()).orElseThrow();
        SkillTagDto savedTag02 = skillTagService.saveSkillTag(new SkillTagDto().setName("tag b")).orElseThrow();

        skillService.saveSkill(
                SkillDto.builder().name("skill a").tags(Set.of(savedTag01)).build()
        ).orElseThrow();
        assertThat(getSkillTaggingCount()).isEqualTo(1);

        assertThatThrownBy(
                () -> skillTagService.deleteSkillTagById(savedTag01.getId())
        ).isInstanceOf(DataIntegrityViolationException.class)
                .message().containsIgnoringCase("fk__skill_tagging__tag");

        assertThat(getSkillTaggingCount()).isEqualTo(1);    // Still the same, no deletion performed
        assertThat(skillTagService.getAllSkillTag()).containsExactlyInAnyOrder(savedTag01, savedTag02);
    }

    // Helpers

    private int getSkillTaggingCount() {
//        return jdbcTemplate.queryForObject("select count(*) from skill_tagging", Integer.class);
//        jdbcClient.sql("SELECT COUNT(*) FROM skill_tagging").query(Integer.class).single();
        return JdbcTestUtils.countRowsInTable(jdbcClient, "skill_tagging");
    }
}