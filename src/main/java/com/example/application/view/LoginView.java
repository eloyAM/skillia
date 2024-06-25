package com.example.application.view;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.Unit;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.ListItem;
import com.vaadin.flow.component.html.UnorderedList;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.component.login.LoginI18n;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Route("login")
@PageTitle("Login")
@AnonymousAllowed
public class LoginView extends VerticalLayout implements BeforeEnterObserver {

    private final LoginForm login = new LoginForm();
    private final LoginI18n.ErrorMessage errorMessage = new LoginI18n.ErrorMessage();
    private final LoginI18n i18n = LoginI18n.createDefault();

    public LoginView() {
        addClassName("login-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        login.setAction("login");
        login.setForgotPasswordButtonVisible(false);

        errorMessage.setTitle("Login error");

        i18n.setErrorMessage(errorMessage);

        login.setI18n(i18n);

        add(logoImage(), new H1("Skillia"), new Text("Skills tracker"), login);

        // TODO INFO only for showcase
        add(createAvailableUsersHint());
    }

    private static Image logoImage() {
        Image logoImg = new Image("icons/icon.png", "Skillia logo");
        logoImg.setHeight(128, Unit.PIXELS);
        logoImg.setWidth(128, Unit.PIXELS);
        return logoImg;
    }

    @Override
    public void beforeEnter(BeforeEnterEvent beforeEnterEvent) {
        // inform the user about an authentication error
        if (beforeEnterEvent.getLocation()
                .getQueryParameters()
                .getParameters()
                .containsKey("error")) {
            ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
            HttpServletRequest request = attr.getRequest();
            String lastLoginErrorMsg = getLoginErrorMessage(request);

            errorMessage.setMessage(lastLoginErrorMsg);
            login.setI18n(i18n);    // Setting the error message is not enough, so we set again the i18n

            login.setError(true);
        }
    }

    private String getLoginErrorMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null &&
                session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof AuthenticationException exception) {
            return exception.getMessage();
        }
        return "Invalid credentials";
    }

    private static Component createAvailableUsersHint() {
        return new Details(
                "Available users hint:",
                new UnorderedList(
                        new ListItem("Password: 1234"),
                        new ListItem("Fully-granted users: hugo.reyes, raquel.huerta"),
                        new ListItem("Regular users: andrea.riquelme, paz.vidal, jacob.smith, ...")
                )
        );
    }
}