package com.ambc.demoServer.tokenSecurity;

public class Constants {
    //5 days
    public static final long tokenExpirationTimeMs = 432_000_000;
    //If the token given by the client as this Bearer, no above verification is needed to verify how the client got the token
    public static final String tokenPrefix = "Bearer ";
    public static final String customJwtTokenHeader = "Jwt-Token";
    //The message send when a token can't be verified
    public static final String tokenCannotBeVerifiedMessage = "Token cannot be verified";
    public static final String getArraysLLC = "Get Arrays, LLC";
    public static final String getArraysAdministration = "User Help App";
    //Hold authorities of the user
    public static final String authorities = "authorities";
    public static final String forbiddenMessage = "You need to log in to access this page";
    public static final String accessDeniedMessage = "You do not have permission to access this page";
    public static final String optionsHttpMethod = "OPTIONS";
    //accessibles URL without to be authenticated
    public static final String[] publicUrls = {"/user/login", "/user/register", "/user/resetPassword/**", "/user/image/**"};
}

