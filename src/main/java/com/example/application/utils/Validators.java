package com.example.application.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Validators {
    public static boolean isNullOrEmpty(String s) {
        return s == null || s.trim().isEmpty();
    }
}
