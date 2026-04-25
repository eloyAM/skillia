package com.example.application.view.components.profile;

import com.example.application.dto.PersonSkillBasicDto;
import com.example.application.security.SecConstants;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static com.example.application.view.utils.ViewUtils.notificationTopCenter;

public class SkillLevelSelector extends MenuBar {

    private static final String CURRENT_LEVEL_CSSCLASS = "current-level";
    private static final String LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME = MenuBarVariant.LUMO_PRIMARY.getVariantName();
    private static final String LUMO_MENU_BAR_ICON_THEME_VARIANT = MenuBarVariant.LUMO_ICON.getVariantName();


    private final Authentication authentication;
    private final String routeUsername;
    private final PersonSkillService personSkillService;

    public SkillLevelSelector(
        @NonNull @Min(1) @Max(5) Integer currentLevel,
        Long skillId,
        Authentication authentication,
        String routeUsername,
        PersonSkillService personSkillService
    ) {
        this.authentication = authentication;
        this.routeUsername = routeUsername;
        this.personSkillService = personSkillService;

        createSkillLevelSelector(currentLevel, skillId);
    }

    private void createSkillLevelSelector(Integer currentLevel, Long skillIid) {
        MenuBar menuBar = this;
        menuBar.addClassName("skill-level-selector");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_ICON);

        // Create menu items for levels 1-5
        List<Integer> levels = PersonSkillService.getLevels();
        int firstLevel = levels.get(0);
        int higherLevel = levels.get(levels.size() - 1);
        List<MenuItem> menuItems = new ArrayList<>(levels.size());
        for (Integer level : levels) {
            String tooltipLevelWithLabel = "%s - %s".formatted(level, PersonSkillService.getLevelName(level));
            String text = String.valueOf(level);    // Compact format - show only the number, not the level label
            MenuItem menuItem = menuBar.addItem(text, tooltipLevelWithLabel);
            menuItems.add(menuItem);

            addSkillSelectorClickListenerIfPermitted(skillIid, menuItem, menuItems);

            // Add CSS classes for styling
            menuItem.addClassNames("level-item", "level-" + level);
            if (level == firstLevel) {
                menuItem.addClassName("first-level");
            } else if (level == higherLevel) {
                menuItem.addClassName("last-level");
            }

            if (level.equals(currentLevel)) {
                menuItem.addThemeNames(LUMO_MENU_BAR_ICON_THEME_VARIANT, LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME);
                menuItem.addClassName(CURRENT_LEVEL_CSSCLASS);
            }
        }
    }

    // Add click listener to handle selection only if my own profile or role allowed
    private void addSkillSelectorClickListenerIfPermitted(Long skillIid, MenuItem menuItem, List<MenuItem> menuItems) {
        if (isMyProfileOrPermittedRole(routeUsername)) {
            menuItem.addClickListener(e -> {
                MenuItem selectedLevelItem = e.getSource();
                String selectedLevel = selectedLevelItem.getText();

                // If there was no previous skill rating, there's no old item
                Optional<MenuItem> oldLevelItem = menuItems.stream()
                    .filter(item -> item.hasClassName(CURRENT_LEVEL_CSSCLASS)).findFirst();

                // No modification if the target is the same level
                if (oldLevelItem.map(selectedLevelItem::equals).orElse(false))
                    return;

                // Update the skill level
                final PersonSkillBasicDto data = new PersonSkillBasicDto(routeUsername, skillIid, Integer.valueOf(selectedLevel));
                PersonSkillBasicDto savedPersonSkill = personSkillService.savePersonSkill(data);
                if (savedPersonSkill == null) {
                    notificationTopCenter("Some error occurred while setting the skill level", false).open();
                    return;
                }

                // Update the view
                selectedLevelItem.addThemeNames(
                    LUMO_MENU_BAR_ICON_THEME_VARIANT, LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME
                );
                selectedLevelItem.addClassName(CURRENT_LEVEL_CSSCLASS);
                oldLevelItem.ifPresent(old -> {
                    old.removeClassName(CURRENT_LEVEL_CSSCLASS);
                    old.removeThemeNames(LUMO_MENU_BAR_PRIMARY_THEME_VARIANT_NAME);
                });
            });
        } else {
            menuItem.addClassName("readonly-level-item");
        }
    }

    private boolean isMyProfileOrPermittedRole(String requestedUsername) {
        return isMyProfile(requestedUsername) || isPermittedRole();
    }

    private boolean isMyProfile(String requestedUsername) {
        String username = authentication.getName();
        return username.equals(requestedUsername);
    }

    private boolean isPermittedRole() {
        Collection<? extends GrantedAuthority> userAuthorities = authentication.getAuthorities();
        SimpleGrantedAuthority rhAuthority = new SimpleGrantedAuthority(SecConstants.ROLE_HR);
        return userAuthorities.contains(rhAuthority);
    }
}
