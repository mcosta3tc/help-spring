package com.ambc.demoServer.tokenSecurity;

import com.ambc.demoServer.user.UserSecuredDetails;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.stream;

@Component
public class JWTTokenProvider {

    private final Logger LOGGER = LoggerFactory.getLogger(getClass());

    //Secret comming from yml file
    @Value("${jwt.secret}")
    private String tokenSecret;

    /*
    //Generate the token
     After user is auth => get users info
     With the infos => generate the token
                            Passing the issuer : our Company name
                                    the audience : what pole of our company is the user (finance, administration, ..)
                                    the date of creation
                                    the Subject : the username (needs to be inique)
                                    the authorities : permissions the user have
                                    date of expiration : get the current of the system and add our expiration time in ms (5d)
                                    sign the token with a secure algo with our secret
     */
    public String generateJWT(UserSecuredDetails userSecuredDetails) {
        String[] currentUserClaims = getCurrentUserClaims(userSecuredDetails);
        return JWT.create()
                .withIssuer(Constants.getArraysLLC)
                .withAudience(Constants.getArraysAdministration)
                .withIssuedAt(new Date())
                .withSubject(userSecuredDetails.getUsername())
                .withArrayClaim(Constants.authorities, currentUserClaims)
                .withExpiresAt(new Date(System.currentTimeMillis() + Constants.tokenExpirationTimeMs))
                .sign(Algorithm.HMAC512(tokenSecret.getBytes()));
    }

    private String[] getCurrentUserClaims(UserSecuredDetails user) {
        List<String> authorisations = new ArrayList<>();
        for (GrantedAuthority grantedAuthority : user.getAuthorities()) {
            authorisations.add(grantedAuthority.getAuthority());
        }
        return authorisations.toArray(new String[0]);
    }

    public List<GrantedAuthority> getAuthoritiesFromToken(String currentToken) {
        String[] userClaimsFromToken = getUserClaimsFromToken(currentToken);
        return stream(userClaimsFromToken).map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    private String[] getUserClaimsFromToken(String currentToken) {
        JWTVerifier jwtVerifier = getJWTVerifier();
        return jwtVerifier.verify(currentToken).getClaim(Constants.authorities).asArray(String.class);
    }


    private JWTVerifier getJWTVerifier() {
        JWTVerifier jwtVerifier;
        try {
            Algorithm algorithm = Algorithm.HMAC512(tokenSecret);
            jwtVerifier = JWT.require(algorithm).withIssuer(Constants.getArraysLLC).build();
        } catch (JWTVerificationException exceptionError) {
            throw new JWTVerificationException(Constants.tokenCannotBeVerifiedMessage);
        }
        return jwtVerifier;
    }

    //Get authentication after verified the token => ? good => tell Spring this user is auth
    public Authentication getAuth(String username, List<GrantedAuthority> authorisations, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(username, null, authorisations);
        usernamePasswordAuthenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return usernamePasswordAuthenticationToken;
    }

    public Boolean isCurrentUserTokenValid(String username, String currentJwtToken) {
        JWTVerifier jwtVerifier = getJWTVerifier();
        return StringUtils.isNotEmpty(username) && !isCurrentUserTokenExpired(jwtVerifier, currentJwtToken);
    }

    public String getSubject(String currentJwtToken) {
        JWTVerifier jwtVerifier = getJWTVerifier();
        return jwtVerifier.verify(currentJwtToken).getSubject();
    }

    private Boolean isCurrentUserTokenExpired(JWTVerifier jwtVerifier, String currentJwtToken) {
        Date expirationDate = jwtVerifier.verify(currentJwtToken).getExpiresAt();
        return expirationDate.before(new Date());
    }
}
