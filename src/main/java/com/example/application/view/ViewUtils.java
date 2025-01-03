package com.example.application.view;

import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

@UtilityClass
public final class ViewUtils {

    public static String getLevelIndicatorSvgPath(Integer level) {
        return String.format("icons/level-%d.svg", level);
    }

    @NonNull
    static Notification notificationTopCenter(String message, boolean success) {
        Notification notification = new Notification(message, 5000,
            Notification.Position.TOP_CENTER);
        notification.addThemeVariants(success
            ? NotificationVariant.LUMO_SUCCESS
            : NotificationVariant.LUMO_ERROR
        );
        return notification;
    }

    @NonNull
    static Notification notificationTopCenter(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 5000,
            Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        return notification;
    }
}
