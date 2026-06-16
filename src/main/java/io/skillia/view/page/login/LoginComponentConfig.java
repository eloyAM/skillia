package io.skillia.view.page.login;

import io.skillia.security.view.LoginComponentProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoginComponentConfig {
    @Bean
    public LoginComponentProvider loginComponentProvider() {
        return () -> LoginView.class;
    }
}
