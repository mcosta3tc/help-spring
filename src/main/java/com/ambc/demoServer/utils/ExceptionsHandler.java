package com.ambc.demoServer.utils;

import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.emailExceptions.NotFoundEmail;
import com.ambc.demoServer.httpResponse.HttpResponseTemplate;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;
import com.auth0.jwt.exceptions.TokenExpiredException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.persistence.NoResultException;
import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.Objects;


@RestControllerAdvice
public class ExceptionsHandler implements ErrorController {
    public static final String accountLocked = "Votre compte est bloqué, veuillez contacter un administrateur";
    public static final String methodNotAllowed = "This request method is not allowed on this endpoint. Please send a '%s' request";
    public static final String serverError = "Une erreur est survenue";
    public static final String wrongCredentials = "Identifiant ou mot de passe incorrect. Veuillez réessayer";
    public static final String accountDisabled = "Votre compté a été bloqué. veuillez contacter votre RH ou un Manager";
    public static final String fileProcessingError = "Une erreur est survenu lors du chargement du fichier";
    public static final String permissionNotEnough = "Vous n'avez pas assez de permissions";

    public static final String errorPath = "/error";

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    //ACCOUNT DISABLED
    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponseTemplate> accountDisabled() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, accountDisabled);
    }

    //WRONG CREDENTIALS
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<HttpResponseTemplate> wrongCredentials() {
        return createHttpResponse(HttpStatus.BAD_REQUEST, wrongCredentials);
    }

    //ACCESS DENIED
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<HttpResponseTemplate> permissionNotEnough() {
        return createHttpResponse(HttpStatus.FORBIDDEN, permissionNotEnough);
    }

    //ACCESS DENIED
    @ExceptionHandler(LockedException.class)
    public ResponseEntity<HttpResponseTemplate> accountLocked() {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, accountLocked);
    }

    //ACCESS DENIED
    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<HttpResponseTemplate> tokenExpired(TokenExpiredException error) {
        return createHttpResponse(HttpStatus.UNAUTHORIZED, error.getMessage().toUpperCase());
    }

    //EMAIL EXISTS
    @ExceptionHandler(ExistsEmail.class)
    public ResponseEntity<HttpResponseTemplate> emailAlreadyExists(ExistsEmail error) {
        return createHttpResponse(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    //USERNAME EXISTS
    @ExceptionHandler(ExistsUserAccountName.class)
    public ResponseEntity<HttpResponseTemplate> emailAlreadyExists(ExistsUserAccountName error) {
        return createHttpResponse(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    //EMAIL NOT FOUND
    @ExceptionHandler(NotFoundEmail.class)
    public ResponseEntity<HttpResponseTemplate> emailNotFound(NotFoundEmail error) {
        return createHttpResponse(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    //USER NOT FOUND
    @ExceptionHandler(NotFoundUser.class)
    public ResponseEntity<HttpResponseTemplate> userNotfound(NotFoundUser error) {
        return createHttpResponse(HttpStatus.BAD_REQUEST, error.getMessage());
    }

    //WRONG REQUEST METHOD
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<HttpResponseTemplate> notSupportedMethod(HttpRequestMethodNotSupportedException error) {
        HttpMethod supportedMethod = Objects.requireNonNull(Objects.requireNonNull(error.getSupportedHttpMethods()).iterator().next());
        return createHttpResponse(HttpStatus.METHOD_NOT_ALLOWED, String.format(methodNotAllowed, supportedMethod));
    }

    //FILE PROCESSING ERROR
    @ExceptionHandler(IOException.class)
    public ResponseEntity<HttpResponseTemplate> iOError(Exception error) {
        LOGGER.error(error.getMessage());
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, serverError);
    }

    //NO RESULT
    @ExceptionHandler(NoResultException.class)
    public ResponseEntity<HttpResponseTemplate> notFoundException(Exception error) {
        LOGGER.error(error.getMessage());
        return createHttpResponse(HttpStatus.NOT_FOUND, serverError);
    }

    //INTERNAL SERVER ERROR
    //If any other exceptions corresponds
    @ExceptionHandler(Exception.class)
    public ResponseEntity<HttpResponseTemplate> internalError(Exception error) {
        LOGGER.error(error.getMessage());
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, serverError);
    }

    @RequestMapping(errorPath)
    public ResponseEntity<HttpResponseTemplate> notFound404() {
        return createHttpResponse(HttpStatus.INTERNAL_SERVER_ERROR, "There is no mapping for this url".toUpperCase());
    }

    private ResponseEntity<HttpResponseTemplate> createHttpResponse(HttpStatus httpResponse, String message) {
        return new ResponseEntity<>(new HttpResponseTemplate(
                httpResponse.value(),
                httpResponse,
                httpResponse.getReasonPhrase().toUpperCase(),
                message.toUpperCase()
        ), httpResponse);
    }
}
