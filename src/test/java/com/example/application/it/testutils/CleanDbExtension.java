package com.example.application.it.testutils;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

public class CleanDbExtension implements BeforeEachCallback, AfterAllCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) {
        deleteTables(extensionContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        deleteTables(extensionContext);
    }

    private static void deleteTables(ExtensionContext extensionContext) {
        JdbcTemplate jdbcTemplate = SpringExtension.getApplicationContext(extensionContext)
            .getBean(JdbcTemplate.class);
        deleteTables(jdbcTemplate);
    }

    private static void deleteTables(JdbcTemplate jdbcTemplate) {
        // Remember to delete join tables first
        // The "person" table is not taken into account
        JdbcTestUtils.deleteFromTables(jdbcTemplate
            , "person_skill"
            , "skill_tagging"
            , "skill_group_skills"
            , "department_skill_groups"
            , "department"
            , "skill_group"
            , "skill_tag"
            , "skill"
        );
    }
}
