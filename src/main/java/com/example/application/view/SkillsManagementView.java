package com.example.application.view;

import com.example.application.security.SecConstants;
import com.example.application.service.SkillService;
import com.example.application.view.components.SkillGroupsTab;
import com.example.application.view.components.SkillTagTab;
import com.example.application.view.components.SkillsViewTab;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@RolesAllowed(SecConstants.HR)
@Route(layout = MainLayout.class, value = "skillsmanagement")
@PageTitle("Skills management")
public class SkillsManagementView extends TabSheet {
    private final SkillService skillService;

    public SkillsManagementView(
        SkillService skillService
    ) {
        this.skillService = skillService;
        //
        createUi();
    }

    private void createUi() {
        setSizeFull();

        SkillsViewTab skillsViewTab = new SkillsViewTab(skillService);

        add(new Tab("Skills"), skillsViewTab);

        SkillTagTab skillTagTab = new SkillTagTab(skillService);
        add(new Tab("Tags"), skillTagTab);

        SkillGroupsTab skillGroupsTab = new SkillGroupsTab(skillService);
        add(new Tab("Groups"), skillGroupsTab);
    }
}