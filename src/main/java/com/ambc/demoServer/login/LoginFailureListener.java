package com.ambc.demoServer.login;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationFailureBadCredentialsEvent;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
public class LoginFailureListener {
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public LoginFailureListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthFail(AuthenticationFailureBadCredentialsEvent event) throws ExecutionException {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof String) {
            String userAccountName = (String) event.getAuthentication().getPrincipal();
            loginAttemptService.addUserToCache(userAccountName);
        }
    }
}
