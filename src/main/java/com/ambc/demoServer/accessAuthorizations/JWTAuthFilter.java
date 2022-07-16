package com.ambc.demoServer.accessAuthorizations;

import com.ambc.demoServer.tokenSecurity.Constants;
import com.ambc.demoServer.tokenSecurity.JWTTokenProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


// == to have a bean when the application starts
@Component
/*
    Auth or not a request
    Fire everytime there is a request only once per request
 */
public class JWTAuthFilter extends OncePerRequestFilter {

    private final JWTTokenProvider jwtTokenProvider;

    public JWTAuthFilter(JWTTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    /*
    When request comes in :
        check if the request methods is Options if so => Set response to OK (Don't need any work === the option method before every request and check for information about the server (if its accepts headers,...) )

        For others methods => Check the authorisation header if the token got prefix define by us || !== null
                                If not let the request continue its flow
                                after that stop the execution of the method (return;)
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getMethod().equalsIgnoreCase(Constants.optionsHttpMethod)) {
            response.setStatus(HttpStatus.OK.value());
        } else {
            // Get the header from the request and we pass the header that we want
            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

            //If the header is null or !start with the token prefix, then we know that is not our auth header => filter and stop the execution of the method
            if (authHeader == null || !authHeader.startsWith(Constants.tokenPrefix)) {
                filterChain.doFilter(request, response);
                return;
            }

            //Otherwise, left the token without the tokenPrefix
            String currentToken = authHeader.substring(Constants.tokenPrefix.length());

            String username = jwtTokenProvider.getSubject(currentToken);

            // Check if the token is valid && check if the user !have an auth token
            if (jwtTokenProvider.isCurrentUserTokenValid(username, currentToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
                List<GrantedAuthority> authorisations = jwtTokenProvider.getAuthoritiesFromToken(currentToken);
                Authentication authentication = jwtTokenProvider.getAuth(username, authorisations, request);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } else {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
