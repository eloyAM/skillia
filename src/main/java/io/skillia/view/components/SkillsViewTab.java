package io.skillia.view.components;

import com.vaadin.componentfactory.Popup;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasComponents;
import com.vaadin.flow.component.HasValue;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.MultiSelectComboBox;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.grid.HeaderRow;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextArea;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.provider.*;
import com.vaadin.flow.theme.lumo.LumoUtility;
import io.skillia.dto.main.SkillDto;
import io.skillia.dto.main.SkillTagDto;
import io.skillia.service.SkillService;
import io.skillia.view.utils.LumoVars;
import io.skillia.view.utils.ValidationConstraints;
import io.skillia.view.utils.ViewUtils;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.vaadin.flow.component.notification.NotificationVariant.*;
import static io.skillia.view.utils.ViewUtils.notificationTopCenter;

@CssImport("./styles/responsive-action-buttons.css")
public class SkillsViewTab extends VerticalLayout {

    private static final String GRID_NAME_COLUMN_NAME = "name";

    private final SkillService skillService;

    private MultiSelectComboBox<SkillTagDto> tagSelectorFilter;

    public SkillsViewTab(
        SkillService skillService
    ) {
        this.skillService = skillService;
        //
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<SkillDto> skillGrid = new Grid<>(SkillDto.class, false);
        skillGrid.addClassName("skills-grid");
        skillGrid.addThemeVariants(
            GridVariant.LUMO_WRAP_CELL_CONTENT
        );

        SkillDtoFilter skillFilter = new SkillDtoFilter();
        SkillDtoDataProvider dataProvider = new SkillDtoDataProvider(skillService);
        ConfigurableFilterDataProvider<SkillDto, Void, SkillDtoFilter> filterDataProvider = dataProvider
            .withConfigurableFilter();

        // Name column -> Name with description bellow
        Grid.Column<SkillDto> nameColumn = skillGrid.addComponentColumn(skillDto -> {
                var nameDiv = new Div(skillDto.getName());
                nameDiv.setTitle(nameDiv.getText());
                nameDiv.addClassNames(LumoUtility.FontWeight.SEMIBOLD);
                var descriptionDiv = new Div(skillDto.getDescription());    // Empty display if description null
                descriptionDiv.addClassNames(LumoUtility.TextColor.SECONDARY, LumoUtility.FontSize.SMALL);
                var result = new VerticalLayout(nameDiv, descriptionDiv);
                result.setSpacing(false);
                return result;
            })
            .setHeader("Name")
            .setKey(GRID_NAME_COLUMN_NAME)
            .setSortable(true)
            .setFlexGrow(2);

        // Tags column -> Badges
        Grid.Column<SkillDto> tagsColumn = skillGrid.addComponentColumn(skillDto -> {
                FlexLayout tagsContainer = skillDto.getTags().stream()
                    .map(SkillTagDto::getName)
                    .map(name -> {
                        Span span = new Span(name);
                        span.setTitle(name);    // Tooltip
                        span.getElement().getThemeList().add("badge contrast pill");
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
            })
            .setHeader("Tags")
            .setKey("tags")
            .setFlexGrow(1);

        // Actions column
        skillGrid.addComponentColumn(selectedSkill -> {
                Dialog editSkillDialog = createEditSkillDialog(selectedSkill, skillGrid);
                ConfirmDialog deleteSkillDialog = createDeleteSkillDialog(selectedSkill, skillGrid);

                // Mobile view: ContextMenu
                Button contextMenuButton = new Button(VaadinIcon.ELLIPSIS_DOTS_V.create());
                contextMenuButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                contextMenuButton.addClassName("mobile-actions");

                ContextMenu contextMenu = new ContextMenu(contextMenuButton);
                contextMenu.setOpenOnClick(true);
                MenuItem editItem = ViewUtils.createIconItem(contextMenu, VaadinIcon.EDIT, "Edit", e -> editSkillDialog.open());
                editItem.getElement().getStyle().set("color", "var(--lumo-primary-text-color)");
                MenuItem deleteItem = ViewUtils.createIconItem(contextMenu, VaadinIcon.TRASH, "Delete", e -> deleteSkillDialog.open());
                deleteItem.getElement().getStyle().set("color", "var(--lumo-error-text-color)");

                // Desktop view: Buttons
                Button editButton = new Button(VaadinIcon.EDIT.create(), e -> editSkillDialog.open());
                editButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
                Button deleteButton = new Button(VaadinIcon.TRASH.create(), e -> deleteSkillDialog.open());
                deleteButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY, ButtonVariant.LUMO_ERROR);
                HorizontalLayout desktopActions = new HorizontalLayout(editButton, deleteButton);
                desktopActions.setSpacing(false);
                desktopActions.addClassName("desktop-actions");

                Div container = new Div(contextMenuButton, desktopActions);
                container.addClassName("actions-container");
                return container;
            })
            .setHeader("Actions")
            .setKey("actions")
            .setAutoWidth(true)
            .setFlexGrow(0);

        skillGrid.setItems(filterDataProvider);

        // Resize listener to adapt column widths
        getElement().executeJs(
            "globalThis.addEventListener('resize', () => { $0.recalculateColumnWidths(); });",
            skillGrid.getElement()
        );

        // Header filters

        HeaderRow headerRow = skillGrid.appendHeaderRow();

        TextField searchTextField = ViewUtils.createFilterTextField("Search", str -> {
            skillFilter.setText(str);
            filterDataProvider.setFilter(skillFilter);
        });
        searchTextField.setTooltipText("Search by name or description");
        headerRow.getCell(nameColumn).setComponent(
            searchTextField
        );

        tagSelectorFilter = ViewUtils.createMultiSelectComboBoxFilter(
            skillService::getAllSkillTagInUse, SkillTagDto::getName, "Filter");
        tagSelectorFilter.addValueChangeListener(e -> {
            skillFilter.setTags(tagSelectorFilter.getSelectedItems());
            filterDataProvider.setFilter(skillFilter);
        });
        tagSelectorFilter.setTooltipText("Matching all selected tags");
        headerRow.getCell(tagsColumn).setComponent(tagSelectorFilter);

        // Result layout
        add(
            createSkillAdderWithDialog(skillGrid),
            skillGrid
        );
    }

    private Component createSkillAdderWithDialog(Grid<SkillDto> skillGrid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create skill");

        Binder<SkillDto> skillBinder = new Binder<>(SkillDto.class);
        FormLayout formLayout = createSkillFormWithBinder(skillBinder);
        formLayout.addClassName("create-skill-form");
        dialog.add(formLayout);

        Button createButton = new Button("Create", e -> {
            SkillDto formDto = new SkillDto();
            final String skillName;
            try {
                skillBinder.writeBean(formDto);
                skillName = formDto.getName();
            } catch (ValidationException ex) {
                notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            Optional<SkillDto> newSkill = skillService.saveSkill(formDto);
            if (newSkill.isPresent()) {
                skillGrid.getDataProvider().refreshAll();
                refreshTagSelectorItems();
                notificationTopCenter(skillNameAndActionMessage(skillName, "created"), true).open();
            } else {
                notificationTopCenter(new Div(
                    new Div("Unable to create the skill"),
                    new Div(" \"" + skillName + "\" "),
                    new Div("It may already exist")
                ), LUMO_WARNING).open();
            }
            skillBinder.getFields().forEach(HasValue::clear);
            dialog.close();
        });
        createButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        createButton.addClassName("create-skill-submit-button");
        createButton.addClickShortcut(Key.ENTER);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addClassNames("create-skill-cancel-button", "cancel-button");
        dialog.getFooter().add(cancelButton, createButton);

        Button addSkillButton =
            new Button("Add Skill", VaadinIcon.PLUS.create(), e -> dialog.open());
        addSkillButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        addSkillButton.addClassName("add-skill-button");
        return addSkillButton;
    }

    private static String skillNameAndActionMessage(String skillName, String action) {
        return "Skill \"" + skillName + "\" " + action;
    }

    private Dialog createEditSkillDialog(SkillDto currentSkill, Grid<SkillDto> grid) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Edit skill \"" + currentSkill.getName() + "\"");

        Binder<SkillDto> skillBinder = new Binder<>(SkillDto.class);
        FormLayout formLayout = createSkillFormWithBinder(skillBinder);
        formLayout.addClassName("edit-skill-form");
        skillBinder.readBean(currentSkill);
        dialog.add(formLayout);

        Button saveButton = new Button("Save", e -> {
            SkillDto formDto = new SkillDto();
            try {
                skillBinder.writeBean(formDto);
                formDto.setId(currentSkill.getId());
            } catch (ValidationException ex) {
                notificationTopCenter("Please fill in the required fields correctly", false).open();
                return;
            }
            String newName = formDto.getName();
            Optional<SkillDto> updatedSkillOpt =
                skillService.saveSkill(formDto);
            if (updatedSkillOpt.isPresent()) {
                notificationTopCenter(skillNameAndActionMessage(newName, "updated"), true).open();
                skillBinder.getFields().forEach(HasValue::clear);
                grid.getDataProvider().refreshAll();    // TODO IMPROVEMENT how to get that single entry updated with `refreshItem` instead of `refreshAll`
                refreshTagSelectorItems();
                dialog.close();
            }
        });
        saveButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        saveButton.addClassName("edit-skill-save-button");
        saveButton.addClickShortcut(Key.ENTER);
        Button cancelButton = new Button("Cancel", e -> dialog.close());
        cancelButton.addClassName("edit-skill-cancel-button");
        dialog.getFooter().add(cancelButton, saveButton);

        return dialog;
    }

    private FormLayout createSkillFormWithBinder(Binder<SkillDto> binder) {
        TextField skillNameTextField = new TextField("Skill name");
        skillNameTextField.setMaxLength(ValidationConstraints.Skill.NAME_MAX_LENGTH);
        skillNameTextField.setRequired(true);

        TextArea descriptionField = new TextArea("Description");
        descriptionField.setMaxLength(ValidationConstraints.Skill.DESCRIPTION_MAX_LENGTH);

        MultiSelectComboBox<SkillTagDto> tagMultiSelectComboBox = ViewUtils
            .createMultiSelectComboBox(skillService::getAllSkillTag, SkillTagDto::getName, null);
        tagMultiSelectComboBox.setLabel("Tags");
        tagMultiSelectComboBox.setRequired(false);
        tagMultiSelectComboBox.setAutoExpand(MultiSelectComboBox.AutoExpandMode.VERTICAL);

        binder.forField(skillNameTextField)
            .asRequired("Name is required")
            .bind(SkillDto::getName, SkillDto::setName);
        binder.forField(descriptionField)
            .bind(SkillDto::getDescription, SkillDto::setDescription);
        binder.forField(tagMultiSelectComboBox)
            .bind(SkillDto::getTags, SkillDto::setTags);

        return new FormLayout(
            skillNameTextField,
            descriptionField,
            tagMultiSelectComboBox
        );
    }

    private ConfirmDialog createDeleteSkillDialog(
        SkillDto selectedSkill,
        Grid<SkillDto> skillGrid
    ) {
        ConfirmDialog confirmDialog = new ConfirmDialog();
        confirmDialog.setHeader("Delete skill \"" + selectedSkill.getName() + "\"");
        confirmDialog.setText("Are you sure you want to permanently delete this item?\r\n"
            + "Any associations with this skill will be removed as well.");
        confirmDialog.setConfirmText("Delete");
        confirmDialog.setConfirmButtonTheme("error primary");
        confirmDialog.addConfirmListener(e -> {
            try {
                skillService.deleteSkillById(selectedSkill.getId());
            } catch (Exception ex) {
                notificationTopCenter("Unexcepted error.", LUMO_ERROR).open();
                throw new RuntimeException(ex);
            }
            notificationTopCenter(
                skillNameAndActionMessage(selectedSkill.getName(), "deleted"), LUMO_SUCCESS).open();
            skillGrid.getDataProvider().refreshAll();
            refreshTagSelectorItems();
        });
        confirmDialog.setCancelable(true);
        return confirmDialog;
    }

    @Setter
    private static class SkillDtoFilter {
        private String text;    // Name or description
        private Set<SkillTagDto> tags;

        private boolean test(SkillDto skillDto) {
            return (matches(skillDto.getName(), text) || matches(skillDto.getDescription(), text))
                && containsAllSelectedTags(skillDto);
        }

        private boolean containsAllSelectedTags(SkillDto skillDto) {
            return tags == null
                || (skillDto.getTags() != null && skillDto.getTags().containsAll(tags));
        }

        private static boolean matches(String value, String searchTerm) {
            return StringUtils.isBlank(searchTerm)
                || (value != null && value.toLowerCase().contains(searchTerm.toLowerCase()));
        }
    }

    private static class SkillDtoDataProvider extends AbstractBackEndDataProvider<SkillDto, SkillDtoFilter> {
        private final SkillService skillService;

        private SkillDtoDataProvider(SkillService skillService) {
            this.skillService = skillService;
        }

        @Override
        protected Stream<SkillDto> fetchFromBackEnd(Query<SkillDto, SkillDtoFilter> query) {
            Stream<SkillDto> stream = skillService.getAllSkill().stream();

            // Filtering
            if (query.getFilter().isPresent()) {
                stream = stream.filter(
                    skill -> query.getFilter().get()
                        .test(skill)
                );
            }

            // Sorting
            if (!query.getSortOrders().isEmpty()) {
                stream = stream.sorted(
                    sortComparator(query.getSortOrders())
                );
            }

            // Pagination
            return stream
                .skip(query.getOffset())
                .limit(query.getLimit());
        }

        @Override
        protected int sizeInBackEnd(Query<SkillDto, SkillDtoFilter> query) {
            return (int) fetchFromBackEnd(query).count();
        }

        private static Comparator<SkillDto> sortComparator(List<QuerySortOrder> sortOrders) {
            return sortOrders.stream()
                .map(sortOrder -> {
                    Comparator<SkillDto> comparator = skillFieldComparator(sortOrder.getSorted());
                    if (sortOrder.getDirection() == SortDirection.DESCENDING) {
                        comparator = comparator.reversed();
                    }
                    return comparator;
                })
                .reduce(Comparator::thenComparing)
                .orElse((p1, p2) -> 0);
        }

        private static Comparator<SkillDto> skillFieldComparator(String sorted) {
            if (sorted.equals(GRID_NAME_COLUMN_NAME)) {
                return Comparator.comparing(SkillDto::getName);
            }
            return (p1, p2) -> 0;
        }
    }

    private void refreshTagSelectorItems() {
        tagSelectorFilter.setItems(skillService.getAllSkillTagInUse());
    }

}
