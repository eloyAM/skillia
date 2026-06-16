package io.skillia.view.page;

import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.*;
import io.skillia.service.DepartmentService;
import io.skillia.service.PersonService;
import io.skillia.service.PersonSkillService;
import io.skillia.service.SkillService;
import io.skillia.view.components.department.members.DepartmentMembersRatingTab;
import io.skillia.view.components.department.skillgroups.DepartmentsSkillGroupsViewTab;
import io.skillia.view.utils.MainLayout;
import io.skillia.view.utils.ViewUtils;
import jakarta.annotation.security.RolesAllowed;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RolesAllowed("HR")
@Route(layout = MainLayout.class, value = "departments")
@PageTitle("Departments")
public class DepartmentsView extends TabSheet implements BeforeEnterObserver {
    private final Map<String, Tab> tabNameToTab = new HashMap<>();
    private boolean isInitialized = false;

    private final DepartmentService departmentService;
    private final SkillService skillService;
    private final PersonService personService;
    private final PersonSkillService personSkillService;

    private DepartmentMembersRatingTab departmentMembersRatingTab;

    public DepartmentsView(
        DepartmentService departmentService,
        SkillService skillService,
        PersonService personService,
        PersonSkillService personSkillService
    ) {
        this.departmentService = departmentService;
        this.skillService = skillService;
        this.personService = personService;
        this.personSkillService = personSkillService;
        //
        createUi();
    }

    private void createUi() {
        setSizeFull();

        var departmentsSkillGroupsViewTab = new DepartmentsSkillGroupsViewTab(departmentService, skillService);
        add(ViewUtils.createTab("Skill groups", tabNameToTab), departmentsSkillGroupsViewTab);

        departmentMembersRatingTab = new DepartmentMembersRatingTab(departmentService, personService, personSkillService);
        add(ViewUtils.createTab("Members rating", tabNameToTab), departmentMembersRatingTab);

        addSelectedChangeListener(e -> {
            Tab selectedTab = e.getSelectedTab();
            if (selectedTab != null) {
                ViewUtils.updateUrlWithTab(selectedTab.getLabel(), isInitialized);
            }
        });
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        QueryParameters queryParams = event.getLocation().getQueryParameters();
        Optional<String> selectedViewParam = queryParams.getSingleParameter(ViewUtils.SELECTED_VIEW_PARAM);
        Optional<String> departmentNameParam = queryParams.getSingleParameter(DepartmentMembersRatingTab.PARAM_DEPARTMENT_NAME);
        selectedViewParam.ifPresent(this::selectTabByName);
        departmentNameParam.ifPresent(departmentMembersRatingTab::selectDepartmentByName);
        isInitialized = true;
    }

    private void selectTabByName(String s) {
        Tab tab = tabNameToTab.get(s);
        if (tab != null) {
            setSelectedTab(tab);
        }
    }
}
