package io.skillia.security.view;

import com.vaadin.flow.component.Component;

public interface LoginComponentProvider {
    Class<? extends Component> getLoginComponentClass();
}
