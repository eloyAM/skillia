package com.example.application.utils;

public class ValidationConstraints {
    private ValidationConstraints() {
    }

    public static class SkillGroup {
        private SkillGroup() {
        }

        public static final int NAME_MAX_LENGTH = 50;
        public static final int DESCRIPTION_MAX_LENGTH = 250;
    }

    public static class Skill {
        private Skill() {
        }

        public static final int NAME_MAX_LENGTH = 70;
        public static final int DESCRIPTION_MAX_LENGTH = 250;
    }

    public static class SkillTag {
        private SkillTag() {
        }

        public static final int NAME_MAX_LENGTH = 70;
    }
}
