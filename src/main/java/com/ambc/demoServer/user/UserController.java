package com.ambc.demoServer.user;

import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.tokenSecurity.JWTTokenProvider;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;
import com.ambc.demoServer.utils.ExceptionsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.ambc.demoServer.tokenSecurity.Constants.customJwtTokenHeader;


@RestController
@RequestMapping(path = {"/", "/user"})
//When an error occurs => ExceptionsHandler Class
public class UserController extends ExceptionsHandler {
    private final UserService userService;
    private final AuthenticationManager manager;
    private final JWTTokenProvider jwtTokenProvider;

    @Autowired
    public UserController(UserService userService, AuthenticationManager manager, JWTTokenProvider jwtTokenProvider) {
        this.userService = userService;
        this.manager = manager;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @PostMapping("/register")
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) throws NotFoundUser, ExistsUserAccountName, ExistsEmail {
        UserEntity newUser = userService.userRegister(user.getUserFirstName(), user.getUserLastName(), user.getUserAccountName(), user.getUserEmail());
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    // pass : fyIRzGf19o
    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity user) {
        loginAuthentication(user.getUserAccountName(), user.getUserPassword());
        UserEntity loginUser = userService.findUserEntityByUserAccountName(user.getUserAccountName());
        UserSecuredDetails userSecuredDetails = new UserSecuredDetails(loginUser);
        HttpHeaders jwtTokenHeader = getJwtTokenHeader(userSecuredDetails);
        return new ResponseEntity<>(loginUser, jwtTokenHeader, HttpStatus.OK);
    }

    private HttpHeaders getJwtTokenHeader(UserSecuredDetails userSecuredDetails) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(customJwtTokenHeader, jwtTokenProvider.generateJWT(userSecuredDetails));
        return headers;
    }

    private void loginAuthentication(String userAccountName, String userPassword) {
        manager.authenticate(new UsernamePasswordAuthenticationToken(userAccountName, userPassword));
    }
}
