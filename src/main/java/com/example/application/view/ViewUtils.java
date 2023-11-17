package com.example.application.view;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ViewUtils {

    public static String getLevelIndicatorSvgPath(Integer level) {
        return String.format("icons/level-%d.svg", level);
    }
}
