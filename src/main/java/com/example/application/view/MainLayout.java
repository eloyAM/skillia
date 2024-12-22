package com.example.application.view;

import com.example.application.security.SecConstants;
import com.example.application.security.SecurityService;
import com.vaadin.flow.component.AttachEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.avatar.Avatar;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@CssImport("./styles/shared-styles.css")
@JsModule("./js/light-dark-theme-chooser.js")
public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final SecurityService securityService;
    private final HorizontalLayout header;
    private boolean isDebugMode = false;

    public MainLayout(@Autowired SecurityService securityService) {
        this.securityService = securityService;
        header = createHeader();
        addToNavbar(header);
        createDrawer();
    }

    private HorizontalLayout createHeader() {
        H1 logo = new H1("Skillia");
        logo.addClassName("app-logo");

        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.addClassName("app-header");
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);

        Div spacer = new Div();
        spacer.getStyle().set("flexGrow", "1");
        header.add(spacer);

        header.add(createThemeSwitcher());

        Authentication authentication = securityService.getAuthentication();
        String username = authentication.getName();
        header.add(createProfileButton(username));

        return header;
    }

    // only for dev purposes
    private static void _addUserInfo(HorizontalLayout header, SecurityService securityService1) {
        //        UserDetails user = securityService.getAuthenticatedUser();
        Authentication authentication = securityService1.getAuthentication();
        header.add(new Div(new Text("user: "
//                + user.getUsername()
                + authentication.getName()
        )));
        header.add(new Div(new Text("roles: "
//                + user.getAuthorities().toString()
                + authentication.getAuthorities().toString()
        )));
    }

    private static Button createThemeSwitcher() {
        Button themeSwitcher = new Button(VaadinIcon.ADJUST.create());
        themeSwitcher.setTooltipText("Switch theme");
        themeSwitcher.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        themeSwitcher.setId("app-theme-switcher");
        themeSwitcher.addClickListener(e -> e.getSource().getElement().executeJs(
            "const theme = document.documentElement.getAttribute('theme');"
                + "const newTheme = theme === 'dark' ? 'light' : 'dark';"
                + "document.documentElement.setAttribute('theme', newTheme);"
                + "localStorage.setItem('app-theme', newTheme);"
        ));
        return themeSwitcher;
    }

    private void createDrawer() {
        addToDrawer(new VerticalLayout(createMenuLink(PersonWithSkillsView.class, "Skills Matrix", VaadinIcon.USERS.create())));

        Authentication authentication = securityService.getAuthentication();
        var userAuthorities = authentication.getAuthorities();
        SimpleGrantedAuthority rhAuthority = new SimpleGrantedAuthority(SecConstants.ROLE_HR);
        if (userAuthorities.contains(rhAuthority)) {
            addToDrawer(new VerticalLayout(createMenuLink(SkillsAssignmentView.class, "Skills Assignment", VaadinIcon.PLUS_CIRCLE_O.create())));
            addToDrawer(new VerticalLayout(createMenuLink(SkillsManagementView.class, "Skills Management", VaadinIcon.MODAL_LIST.create())));
            addToDrawer(new VerticalLayout(createMenuLink(SkillTagView.class, "Skill tags Management", VaadinIcon.TAG.create())));
        }
    }

    private static RouterLink createMenuLink(Class<? extends Component> viewClass, String caption, Icon icon) {
        final RouterLink routerLink = new RouterLink(viewClass);
        routerLink.setClassName("menu-link");
        routerLink.add(icon, new Span(caption));
        return routerLink;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        final String debugMode = event.getLocation().getQueryParameters().getSingleParameter("debugMode").orElse("false");
        if ("true".equals(debugMode)) {
            isDebugMode = true;
        }
    }

    @Override
    protected void onAttach(AttachEvent attachEvent) {
        if (isDebugMode) {
            addToDrawer(new VerticalLayout(new RouterLink("[DEBUG] Users list", PersonGridView.class)));
            addToDrawer(new VerticalLayout(new RouterLink("[DEBUG] Person Skill Grid", PersonSkillGridView.class)));
            addToDrawer(new VerticalLayout(new RouterLink("[DEBUG] Skill Grid", SkillGridView.class)));
            _addUserInfo(header, securityService);
        }
    }

    private Component createProfileButton(String username) {
        Avatar avatar = new Avatar();

        MenuBar menuBar = new MenuBar();
        menuBar.setId("app-profile-element");
        menuBar.addThemeVariants(MenuBarVariant.LUMO_TERTIARY_INLINE);

        MenuItem menuItem = menuBar.addItem(avatar);
        menuBar.setTooltipText(menuItem, username);
        SubMenu subMenu = menuItem.getSubMenu();

        Button logOutButton = new Button("Log out", VaadinIcon.SIGN_OUT.create()
                , e -> securityService.logout());
        logOutButton.setId("app-logout-button");

        subMenu.addItem(username);
        subMenu.add(logOutButton);

        return menuBar;
    }
}
