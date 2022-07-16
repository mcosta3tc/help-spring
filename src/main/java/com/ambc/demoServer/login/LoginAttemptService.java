package com.ambc.demoServer.login;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {
    //login tries the user has
    private static final int MAX_ATTEMPTS = 5;

    private static final int ATTEMPTS_INCREMENT_NBR = 1;

    //Key / Value
    /*
    ex: user || attempts
     */
    private final LoadingCache<String, Integer> loginAttemptCache;

    public LoginAttemptService() {
        super();
        loginAttemptCache = CacheBuilder.newBuilder()
                .expireAfterWrite(15, TimeUnit.MINUTES)
                //max of key / value (users)
                .maximumSize(100)
                .build(new CacheLoader<String, Integer>() {
                    public Integer load(String key) {
                        return 0;
                    }
                });
    }

    public void removeUserFromCache(String userAccountName) {
        loginAttemptCache.invalidate(userAccountName);
    }

    public void addUserToCache(String userAccountName) {
        int tries = 0;
        try {
            tries = ATTEMPTS_INCREMENT_NBR + loginAttemptCache.get(userAccountName);
        } catch (ExecutionException error) {
            error.printStackTrace();
        }
        loginAttemptCache.put(userAccountName, tries);
    }

    public Boolean reachMaxAttempts(String userAccountName) {
        try {
            return loginAttemptCache.get(userAccountName) >= MAX_ATTEMPTS;
        } catch (ExecutionException error) {
            error.printStackTrace();
        }
        return false;
    }
}


