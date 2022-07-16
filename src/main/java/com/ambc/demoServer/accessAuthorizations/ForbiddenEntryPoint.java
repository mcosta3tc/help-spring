package com.ambc.demoServer.accessAuthorizations;

import com.ambc.demoServer.httpResponse.HttpResponseTemplate;
import com.ambc.demoServer.tokenSecurity.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
// If a user is not auth and try to get the app => 403
public class ForbiddenEntryPoint extends Http403ForbiddenEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {

        HttpResponseTemplate httpResponse = new HttpResponseTemplate(HttpStatus.FORBIDDEN.value(), HttpStatus.FORBIDDEN, HttpStatus.FORBIDDEN.getReasonPhrase().toUpperCase(), Constants.forbiddenMessage);

        //set the content of the response to json
        response.setContentType(APPLICATION_JSON_VALUE);

        //set status
        response.setStatus(HttpStatus.FORBIDDEN.value());

        //Stream our HttpResponseTemplate httpResponse and map the obj the HttpServletResponse response
        OutputStream outputStream = response.getOutputStream();
        ObjectMapper mapper = new ObjectMapper();
        mapper.writeValue(outputStream, httpResponse);
        outputStream.flush();
    }
}
