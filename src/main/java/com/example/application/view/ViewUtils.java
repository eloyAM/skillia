package com.example.application.view;

import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.dto.SkillAndPeopleWithLevel;
import com.example.application.utils.Validators;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ItemLabelGenerator;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBoxVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

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

    @NonNull
    static Notification notificationTopCenter(Component component, NotificationVariant variant) {
        Notification notification = new Notification(component);
        notification.setDuration(5000);
        notification.setPosition(Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        return notification;
    }

    public static TextField createFilterTextField(
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

    public static <T> MultiSelectComboBox<T> createMultiSelectComboBoxFilter(
        Supplier<List<T>> itemsSupplier, ItemLabelGenerator<T> itemLabelGenerator, String placeholder
    ) {
        MultiSelectComboBox<T> selector = new MultiSelectComboBox<>();
        selector.setPlaceholder(placeholder);
        selector.setClearButtonVisible(true);
        selector.addThemeVariants(MultiSelectComboBoxVariant.LUMO_SMALL);
        selector.setSelectedItemsOnTop(true);
        selector.setWidthFull();
        selector.setMaxWidth("100%");
        selector.setItemLabelGenerator(itemLabelGenerator);
        selector.setItems(itemsSupplier.get());
        return selector;
    }


    private static Component createSkillLevelIndicator(String label, Integer skillLevel) {
        var levelIndicatorComponent = new Div(
            new Div(new Text(label)),
            new Div(new Image(getLevelIndicatorSvgPath(skillLevel), "level " + skillLevel))
        );
        levelIndicatorComponent.addClassName("skill-level-indicator-with-label");
        return levelIndicatorComponent;
    }

    public static ComponentRenderer<? extends Component, PersonWithSkillsDto> skillLevelIndicatorRendererForPersonWithSkills() {
        return new ComponentRenderer<>(personWithSkills -> {
            var container = new FlexLayout();
            for (var personSkill : personWithSkills.getSkills()) {
                String label = personSkill.getSkill().getName();
                Integer skillLevel = personSkill.getLevel();
                container.add(createSkillLevelIndicator(label, skillLevel));
            }
            container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            container.getStyle()
                .set("gap", "var(--lumo-space-xl)")
                .set("padding-top", "var(--lumo-space-s)")
                .set("padding-bottom", "var(--lumo-space-s)");
            return container;
        });
    }

    public static ComponentRenderer<? extends Component, SkillAndPeopleWithLevel> skillLevelIndicatorRendererForSkillAndPeopleWithLevel() {
        return new ComponentRenderer<>(skillAndPeopleWithLevel -> {
            var container = new FlexLayout();
            for (var personAndLevel : skillAndPeopleWithLevel.getPeopleWithLevel()) {
                var person = personAndLevel.getPerson();
                Integer skillLevel = personAndLevel.getLevel();
                String label = Validators.isNullOrEmpty(person.getFullName())
                    ? person.getUsername() : person.getFullName();
                container.add(createSkillLevelIndicator(label, skillLevel));
            }
            container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
            container.getStyle()
                .set("gap", "var(--lumo-space-xl)")
                .set("padding-top", "var(--lumo-space-s)")
                .set("padding-bottom", "var(--lumo-space-s)");
            return container;
        });
    }
}
