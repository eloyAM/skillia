package com.example.application.view;

import com.example.application.view.components.AccessDenied;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.router.*;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletResponse;

@Tag(Tag.DIV)
@ParentLayout(MainLayout.class)
@PermitAll
public class AccessDeniedExceptionHandler extends Component implements HasErrorParameter<AccessDeniedException> {
    @Override
    public int setErrorParameter(BeforeEnterEvent beforeEnterEvent, ErrorParameter<AccessDeniedException> errorParameter) {
        getElement().setChild(0, new AccessDenied().getElement());
        return HttpServletResponse.SC_FORBIDDEN;
    }
}
