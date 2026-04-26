package com.example.application.view.components.profile;

import com.example.application.security.SecConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class ProfilePermissionsHelper {
    private final boolean isMyProfile;
    private final boolean isPermittedRole;

    public ProfilePermissionsHelper(Authentication authentication, String requestedUsername) {
        this.isMyProfile = authentication.getName()
            .equals(requestedUsername);
        this.isPermittedRole = authentication.getAuthorities()
            .contains(new SimpleGrantedAuthority(SecConstants.ROLE_HR));
    }

    public boolean isMyProfileOrPermittedRole() {
        return isMyProfile || isPermittedRole;
    }

}
