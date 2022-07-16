package com.ambc.demoServer.user.authorities;

public class Authority {
    public static final String[] userAuthorisations = {"user:read"};
    public static final String[] hrAuthorisations = {"user:read", "user:update"};
    public static final String[] managerAuthorisations = {"user:read", "user:update"};
    public static final String[] adminAuthorisations = {"user:read", "user:update", "user:create"};
    public static final String[] sudoAdminAuthorisations = {"user:read", "user:update", "user:create", "user:delete"};
}
