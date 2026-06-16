package io.skillia.view.page;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import io.skillia.security.SecConstants;
import io.skillia.service.SkillService;
import io.skillia.view.components.SkillGroupsTab;
import io.skillia.view.components.SkillTagTab;
import io.skillia.view.components.SkillsViewTab;
import io.skillia.view.utils.MainLayout;
import io.skillia.view.utils.ViewUtils;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashMap;
import java.util.Map;

import static io.skillia.view.utils.ViewUtils.createTab;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skillsmanagement")
@PageTitle("Skills management")
public class SkillsManagementView extends TabSheet implements BeforeEnterObserver {
    private final Map<String, Tab> tabNameToTab = new HashMap<>();
    private boolean isInitialized = false;

    private final SkillService skillService;

    public SkillsManagementView(
        SkillService skillService
    ) {
        this.skillService = skillService;
        // Create tabs + add listener to update the URL based on the selected tab
        createUi();
        addSelectedChangeListener(e -> {
            Tab selectedTab = e.getSelectedTab();
            if (selectedTab != null) {
                ViewUtils.updateUrlWithTab(selectedTab.getLabel(), isInitialized);
            }
        });
    }

    private void createUi() {
        setSizeFull();

        SkillsViewTab skillsViewTab = new SkillsViewTab(skillService);
        add(createTab("Skills", tabNameToTab), skillsViewTab);

        SkillTagTab skillTagTab = new SkillTagTab(skillService);
        add(createTab("Tags", tabNameToTab), skillTagTab);

        SkillGroupsTab skillGroupsTab = new SkillGroupsTab(skillService);
        add(createTab("Groups", tabNameToTab), skillGroupsTab);
    }

    // Select the tab based on the URL as we load the page -> access directly to the tab / remember the tab on refresh
    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        event.getLocation().getQueryParameters()
            .getSingleParameter(ViewUtils.SELECTED_VIEW_PARAM).ifPresent(this::selectTabByName);
        isInitialized = true;
    }

    private void selectTabByName(String s) {
        Tab tab = tabNameToTab.get(s);
        if (tab != null) {
            setSelectedTab(tab);
        }
    }

}