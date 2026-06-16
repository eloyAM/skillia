package io.skillia.view.components.profile;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import io.skillia.dto.main.PersonDto;

import java.util.Objects;

import static io.skillia.view.utils.ViewUtils.createAndInitialize;

public class UserDetailsCard extends Div {

    public UserDetailsCard(PersonDto personDto) {
        // Create a compact user details card
        this.add(createUserLayout(personDto));
        this.addClassName("user-details-card");
        this.getStyle()
            .set("background", "var(--lumo-contrast-5pct)")
            .set("border-radius", "var(--lumo-border-radius-m)")
            .set("padding", "var(--lumo-space-m)")
            .set("margin-bottom", "var(--lumo-space-l)");
    }

    private Component createUserLayout(PersonDto personDto) {
        H2 fullName = new H2(Objects.requireNonNullElse(personDto.getFullName(), "Unknown User"));
        fullName.getStyle().set("margin", "0").set("color", "var(--lumo-primary-text-color)");

        Span usernameSpan = new Span("@" + personDto.getUsername());
        usernameSpan.getStyle().set("color", "var(--lumo-secondary-text-color)").set("font-size", "var(--lumo-font-size-s)");

        var emailJobTitleAndDepartment = new Div(
            new Span("📧 " + Objects.requireNonNullElse(personDto.getEmail(), "No email")),
            new Div(
                new Span("💼 " + Objects.requireNonNullElse(personDto.getTitle(), "No title")),
                // Spacer
                createAndInitialize(new Span(), span -> span.getStyle().setPaddingRight("var(--lumo-space-m)")),
                new Span("🏢 " + Objects.requireNonNullElse(personDto.getDepartment(), "No department"))
            )
        );
        emailJobTitleAndDepartment.getStyle()
            .set("font-size", "var(--lumo-font-size-s)")
            .set("display", "flex")
            .set("flex-wrap", "wrap")
            .set("flex-direction", "row")
            .set("column-gap", "var(--lumo-space-l")
            .set("row-gap", "var(--lumo-space-xs)");

        // User details
        VerticalLayout detailsLayout = new VerticalLayout(
            fullName, usernameSpan, emailJobTitleAndDepartment
        );
        detailsLayout.setPadding(false);
        detailsLayout.setSpacing(false);

        // Create avatar and user info layout
        HorizontalLayout userInfoLayout = new HorizontalLayout(detailsLayout);
        userInfoLayout.setAlignItems(FlexComponent.Alignment.CENTER);
        userInfoLayout.setSpacing(true);
        return userInfoLayout;
    }
}
