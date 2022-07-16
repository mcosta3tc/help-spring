package com.ambc.demoServer.emailExceptions;

public class NotFoundEmail extends Exception {
    public NotFoundEmail(String message) {
        super(message);
    }
}
