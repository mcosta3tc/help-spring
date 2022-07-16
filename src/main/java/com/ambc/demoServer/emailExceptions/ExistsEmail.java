package com.ambc.demoServer.emailExceptions;

public class ExistsEmail extends Exception {
    public ExistsEmail(String message) {
        super(message);
    }
}
