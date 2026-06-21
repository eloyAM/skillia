package io.skillia.view.config;

import io.skillia.security.view.LoginComponentProvider;
import io.skillia.view.page.LoginView;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginComponentConfig {
    @Bean
    public LoginComponentProvider loginComponentProvider() {
        return () -> LoginView.class;
    }
}
