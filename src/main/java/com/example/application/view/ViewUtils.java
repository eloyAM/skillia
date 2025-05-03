package com.example.application.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.function.Consumer;

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

    public static Component createFilterTextField(
        String placeHolderText,
        Consumer<String> filterChangeConsumer
    ) {
        TextField textField = new TextField();
        textField.setPrefixComponent(VaadinIcon.SEARCH.create());
        textField.setPlaceholder(placeHolderText);
        textField.setValueChangeMode(ValueChangeMode.EAGER);
        textField.setClearButtonVisible(true);
        textField.addThemeVariants(TextFieldVariant.LUMO_SMALL);
        textField.setWidthFull();
        textField.setMaxWidth("100%");
        textField.setValueChangeTimeout(800);
        textField.addValueChangeListener(
            e -> filterChangeConsumer.accept(e.getValue())
        );
        return textField;
    }
}
