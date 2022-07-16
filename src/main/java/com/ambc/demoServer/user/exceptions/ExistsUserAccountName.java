package com.ambc.demoServer.user.exceptions;

public class ExistsUserAccountName extends Exception {
    public ExistsUserAccountName(String message) {
        super(message);
    }
}
