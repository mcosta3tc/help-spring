package com.ambc.demoServer.user.roles;

import com.ambc.demoServer.user.authorities.Authority;

public enum Role {
    ROLE_USER(Authority.userAuthorisations),
    ROLE_HR(Authority.hrAuthorisations),
    ROLE_MANAGER(Authority.managerAuthorisations),
    ROLE_ADMIN(Authority.adminAuthorisations),
    ROLE_SUDO_ADMIN(Authority.sudoAdminAuthorisations);

    private final String[] authorities;

    Role(String... authorities) {
        this.authorities = authorities;
    }

    public String[] getAuthorities() {
        return authorities;
    }
}
