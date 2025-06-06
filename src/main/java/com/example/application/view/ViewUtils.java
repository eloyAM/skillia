package com.example.application.view;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonWithLevelDto;
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
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@UtilityClass
public final class ViewUtils {

    public static String getLevelIndicatorSvgPath(Integer level) {
        return String.format("icons/level-%d.svg", level);
    }

    @NonNull
    public static Notification notificationTopCenter(String message, boolean success) {
        Notification notification = new Notification(message, 5000,
            Notification.Position.TOP_CENTER);
        notification.addThemeVariants(success
            ? NotificationVariant.LUMO_SUCCESS
            : NotificationVariant.LUMO_ERROR
        );
        return notification;
    }

    @NonNull
    public static Notification notificationTopCenter(String message, NotificationVariant variant) {
        Notification notification = new Notification(message, 5000,
            Notification.Position.TOP_CENTER);
        notification.addThemeVariants(variant);
        return notification;
    }

    @NonNull
    public static Notification notificationTopCenter(Component component, NotificationVariant variant) {
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

    private static <T> FlexLayout createSkillLevelIndicatorContainer(
        Iterable<T> items,
        Function<T, String> labelProvider,
        ToIntFunction<T> levelProvider
    ) {
        var container = new FlexLayout();
        items.forEach(item -> {
            String label = labelProvider.apply(item);
            Integer level = levelProvider.applyAsInt(item);
            container.add(createSkillLevelIndicator(label, level));
        });
        container.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        container.getStyle()
            .set("row-gap", "var(--lumo-space-l)")
            .set("column-gap", "var(--lumo-space-xl)");
        return container;
    }

    public static ComponentRenderer<Component, PersonWithSkillsDto> skillLevelIndicatorRendererForPersonWithSkills() {
        return new ComponentRenderer<>(personWithSkills -> {
            var items = personWithSkills.getSkills();
            return createSkillLevelIndicatorContainer(
                items,
                personSkill -> personSkill.getSkill().getName(),
                AcquiredSkillDto::getLevel
            );
        });
    }

    public static ComponentRenderer<Component, SkillAndPeopleWithLevel> skillLevelIndicatorRendererForSkillAndPeopleWithLevel() {
        return new ComponentRenderer<>(skillAndPeopleWithLevel -> {
            var items = skillAndPeopleWithLevel.getPeopleWithLevel();
            return createSkillLevelIndicatorContainer(
                items,
                personAndLevel -> {
                    var person = personAndLevel.getPerson();
                    return Validators.isNullOrEmpty(person.getFullName())
                        ? person.getUsername() : person.getFullName();
                },
                PersonWithLevelDto::getLevel
            );
        });
    }

    // Create and initialize a component with a single statement
    public static <T extends Component> T createAndInitialize(T component, Consumer<T> initializer) {
        initializer.accept(component);
        return component;
    }
}
