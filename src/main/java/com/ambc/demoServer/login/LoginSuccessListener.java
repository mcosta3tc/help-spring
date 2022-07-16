package com.ambc.demoServer.login;

import com.ambc.demoServer.user.UserSecuredDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.stereotype.Component;

@Component
public class LoginSuccessListener {
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public LoginSuccessListener(LoginAttemptService loginAttemptService) {
        this.loginAttemptService = loginAttemptService;
    }

    @EventListener
    public void onAuthSuccess(AuthenticationSuccessEvent event) {
        Object principal = event.getAuthentication().getPrincipal();
        if (principal instanceof UserSecuredDetails) {
            UserSecuredDetails user = (UserSecuredDetails) event.getAuthentication().getPrincipal();
            loginAttemptService.removeUserFromCache(user.getUsername());
        }
    }
}
