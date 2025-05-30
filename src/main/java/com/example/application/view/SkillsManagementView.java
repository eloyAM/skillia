package com.example.application.view;

import com.example.application.security.SecConstants;
import com.example.application.service.SkillGroupService;
import com.example.application.service.SkillService;
import com.example.application.service.SkillTagService;
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
    private final SkillTagService skillTagService;
    private final SkillGroupService skillGroupService;

    public SkillsManagementView(SkillService skillService, SkillTagService skillTagService, SkillGroupService skillGroupService) {
        this.skillService = skillService;
        this.skillTagService = skillTagService;
        this.skillGroupService = skillGroupService;
        //
        createUi();
    }

    private void createUi() {
        setSizeFull();

        SkillsViewTab skillsViewTab = new SkillsViewTab(skillService, skillTagService);

        add(new Tab("Skills"), skillsViewTab);

        SkillTagTab skillTagTab = new SkillTagTab(skillTagService);
        add(new Tab("Tags"), skillTagTab);

        SkillGroupsTab skillGroupsTab = new SkillGroupsTab(skillGroupService, skillService);
        add(new Tab("Groups"), skillGroupsTab);
    }
}