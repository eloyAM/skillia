package io.skillia.view.error;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.*;
import io.skillia.view.utils.MainLayout;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;

@Tag(Tag.DIV)
@ParentLayout(MainLayout.class)
@PermitAll
public class AccessDeniedExceptionHandler extends Component implements HasErrorParameter<AccessDeniedException> {
    @Override
    public int setErrorParameter(BeforeEnterEvent beforeEnterEvent, ErrorParameter<AccessDeniedException> errorParameter) {
        getElement().setChild(0, accessDeniedComponent().getElement());
        return HttpServletResponse.SC_FORBIDDEN;
    }

    private static VerticalLayout accessDeniedComponent() {
        VerticalLayout layout = new VerticalLayout();
        layout.setAlignItems(FlexComponent.Alignment.CENTER);
        Icon warningIcon = VaadinIcon.LOCK.create();
        warningIcon.setSize("100px");
        layout.add(
            warningIcon,
            new H1("Access denied")
        );
        return layout;
    }

}
