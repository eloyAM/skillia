package com.example.application.it.testutils;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.jdbc.JdbcTestUtils;

public class CleanDbExtension implements BeforeEachCallback, AfterAllCallback {
    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        deleteTables(extensionContext);
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        deleteTables(extensionContext);
    }

    private static void deleteTables(ExtensionContext extensionContext) {
        JdbcTemplate jdbcTemplate = SpringExtension.getApplicationContext(extensionContext)
                .getBean(JdbcTemplate.class);
        deleteTables(jdbcTemplate);
    }

    private static void deleteTables(JdbcTemplate jdbcTemplate) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate
                , "person_skill"
                , "skill"
                , "skill_tagging"
                , "skill_tag"
        );
    }
}
