package com.example.application.view.utils;

import com.example.application.dto.*;
import com.example.application.utils.Validators;
import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.combobox.MultiSelectComboBoxVariant;
import com.vaadin.flow.component.contextmenu.HasMenuItems;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.renderer.ComponentRenderer;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.Location;
import com.vaadin.flow.router.QueryParameters;
import lombok.experimental.UtilityClass;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.ToIntFunction;

@UtilityClass
public final class ViewUtils {

    public static final String SELECTED_VIEW_PARAM = "selectedView";

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

    // Alias
    public static <T> MultiSelectComboBox<T> createMultiSelectComboBox(
        Supplier<List<T>> itemsSupplier, ItemLabelGenerator<T> itemLabelGenerator, String placeholder
    ) {
        return createMultiSelectComboBoxFilter(itemsSupplier, itemLabelGenerator, placeholder);
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
        selector.setAutoExpand(MultiSelectComboBox.AutoExpandMode.BOTH);
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

    public static MenuItem createIconItem(
        HasMenuItems menu, VaadinIcon vaadinIcon, String label,
        ComponentEventListener<ClickEvent<MenuItem>> onClick
    ) {
        Icon icon = new Icon(vaadinIcon);
        icon.getStyle().setMarginRight("var(--lumo-space-m");
        MenuItem item = menu.addItem(icon, onClick);
        item.add(new Text(label));
        item.setAriaLabel(label);
        return item;
    }

    public static FlexLayout skillsAsBadges(SkillGroupDto group) {
        FlexLayout tagsContainer = group.getSkills().stream()
            .map(SkillDto::getName)
            .map(name -> {
                Span span = new Span(name);
                span.setTitle(name);    // Tooltip
                span.getElement().getThemeList().add("badge contrast");
                Popup popup = new Popup();
                popup.setTarget(span.getElement());
                popup.setHeaderTitle(name);
                return new Span(span, popup);
            })
            .collect(FlexLayout::new, HasComponents::add, HasComponents::add);
        tagsContainer.setFlexWrap(FlexLayout.FlexWrap.WRAP);
        tagsContainer.getStyle()
            .set("gap", LumoVars.LUMO_SPACE_S)
            .set("padding-top", LumoVars.LUMO_SPACE_S)
            .set("padding-bottom", LumoVars.LUMO_SPACE_S);
        return tagsContainer;
    }

    // Updates the page URL, adding/updating the query param
    public static void updateQueryParameter(String paramName, String value) {
        Location currentLocation = UI.getCurrent().getActiveViewLocation();
        Map<String, List<String>> params = new HashMap<>(currentLocation.getQueryParameters().getParameters());
        params.put(paramName, List.of(value));
        QueryParameters qp = new QueryParameters(params);
        Location newLocation = new Location(currentLocation.getPath(), qp);
        // Update without reloading the page
        UI.getCurrent().getPage().getHistory().replaceState(null, newLocation);
    }

    public static void updateUrlWithTab(String tabName, boolean shouldUpdate) {
        if (!shouldUpdate) {
            return; // Avoid updating URL before initial navigation
        }
        if (UI.getCurrent().getActiveViewLocation() != null) {
            updateQueryParameter(SELECTED_VIEW_PARAM, tabName);
        }
    }

    public static Tab createTab(String label, Map<String, Tab> tabNameToTab) {
        Tab tab = new Tab(label);
        tabNameToTab.put(label, tab);
        return tab;
    }
}
