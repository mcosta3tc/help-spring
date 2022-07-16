package com.ambc.demoServer.accessAuthorizations;

import com.ambc.demoServer.httpResponse.HttpResponseTemplate;
import com.ambc.demoServer.tokenSecurity.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Component
public class AccessDeniedHandler implements org.springframework.security.web.access.AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException exception) throws IOException, ServletException {

        HttpResponseTemplate httpResponse = new HttpResponseTemplate(HttpStatus.UNAUTHORIZED.value(), HttpStatus.UNAUTHORIZED, HttpStatus.UNAUTHORIZED.getReasonPhrase().toUpperCase(), Constants.accessDeniedMessage);

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
