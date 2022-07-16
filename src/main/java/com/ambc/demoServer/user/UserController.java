package com.ambc.demoServer.user;

import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.emailExceptions.NotFoundEmail;
import com.ambc.demoServer.httpResponse.HttpResponseTemplate;
import com.ambc.demoServer.tokenSecurity.JWTTokenProvider;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;
import com.ambc.demoServer.utils.ExceptionsHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import static com.ambc.demoServer.tokenSecurity.Constants.customJwtTokenHeader;
import static com.ambc.demoServer.user.constants.ProfilePictureFileConstants.*;
import static org.springframework.http.MediaType.IMAGE_JPEG_VALUE;


@RestController
@RequestMapping(path = {"/", "/user"})
//When an error occurs => ExceptionsHandler Class
public class UserController extends ExceptionsHandler {
    public static final String EMAIL_SENT = "An Email with a new password was sent to : ";
    public static final String USER_DELETED_SUCCESSFULLY = "User deleted successfully";
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
    public ResponseEntity<UserEntity> register(@RequestBody UserEntity user) throws NotFoundUser, ExistsUserAccountName, ExistsEmail, MessagingException {
        UserEntity newUser = userService.userRegister
                (
                        user.getUserFirstName(),
                        user.getUserLastName(),
                        user.getUserAccountName(),
                        user.getUserEmail()
                );
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/login")
    public ResponseEntity<UserEntity> login(@RequestBody UserEntity user) {
        loginAuthentication(user.getUserAccountName(), user.getUserPassword());
        UserEntity loginUser = userService.findUserEntityByUserAccountName(user.getUserAccountName());
        UserSecuredDetails userSecuredDetails = new UserSecuredDetails(loginUser);
        HttpHeaders jwtTokenHeader = getJwtTokenHeader(userSecuredDetails);
        return new ResponseEntity<>(loginUser, jwtTokenHeader, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<UserEntity> addNewUser
            (
                    @RequestParam("userFirstName") String userFirstName,
                    @RequestParam("userLastName") String userLastName,
                    @RequestParam("userAccountName") String userAccountName,
                    @RequestParam("userEmail") String userEmail,
                    @RequestParam("userRole") String userRole,
                    @RequestParam("isUserConnected") String isUserConnected,
                    @RequestParam("isUserNotBanned") String isUserNotBanned,
                    @RequestParam(value = "userProfilePictureLink", required = false) MultipartFile userProfilePictureLink
            ) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException {
        UserEntity newUser = userService.addNewUserEntity
                (
                        userFirstName,
                        userLastName,
                        userAccountName,
                        userEmail,
                        userRole,
                        Boolean.parseBoolean(isUserConnected),
                        Boolean.parseBoolean(isUserNotBanned),
                        userProfilePictureLink
                );
        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/update")
    public ResponseEntity<UserEntity> updateUser
            (
                    @RequestParam("currentUserFirstName") String currentUserFirstName,
                    @RequestParam("userFirstName") String userFirstName,
                    @RequestParam("userLastName") String userLastName,
                    @RequestParam("userAccountName") String userAccountName,
                    @RequestParam("userEmail") String userEmail,
                    @RequestParam("userRole") String userRole,
                    @RequestParam("isUserConnected") String isUserConnected,
                    @RequestParam("isUserNotBanned") String isUserNotBanned,
                    @RequestParam(value = "userProfilePictureLink", required = false) MultipartFile userProfilePictureLink
            ) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException {
        UserEntity updateUserEntity = userService.updateUserEntity
                (
                        currentUserFirstName,
                        userFirstName,
                        userLastName,
                        userAccountName,
                        userEmail,
                        userRole,
                        Boolean.parseBoolean(isUserConnected),
                        Boolean.parseBoolean(isUserNotBanned),
                        userProfilePictureLink
                );
        return new ResponseEntity<>(updateUserEntity, HttpStatus.OK);
    }

    @GetMapping("/find/{userAccountName}")
    public ResponseEntity<UserEntity> getUser(@PathVariable("userAccountName") String userAccountName) {
        UserEntity user = userService.findUserEntityByUserAccountName(userAccountName);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/list")
    public ResponseEntity<List<UserEntity>> getUsers() {
        List<UserEntity> allUsers = userService.getAllUsers();
        return new ResponseEntity<>(allUsers, HttpStatus.OK);
    }

    @GetMapping("/resetPassword/{userEmail}")
    public ResponseEntity<HttpResponseTemplate> resetUserPassword(@PathVariable("userEmail") String userEmail) throws NotFoundEmail, MessagingException {
        userService.resetUserPassword(userEmail);
        return customResponse(HttpStatus.OK, EMAIL_SENT + userEmail);
    }

    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAnyAuthority('user:delete')")
    public ResponseEntity<HttpResponseTemplate> deleteUser(@PathVariable("id") long id) {
        userService.deleteUserEntity(id);
        return customResponse(HttpStatus.NO_CONTENT, USER_DELETED_SUCCESSFULLY);
    }

    @PostMapping("/updateProfileImage")
    public ResponseEntity<UserEntity> updateProfileImage
            (
                    @RequestParam("userAccountName") String userAccountName,
                    @RequestParam(value = "userProfilePictureLink") MultipartFile userProfilePictureLink
            ) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException {
        UserEntity user = userService.updateUserProfileImage(userAccountName, userProfilePictureLink);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping(path = "/image/{userAccountName}/{fileName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getProfileImage(@PathVariable("userAccountName") String userAccountName, @PathVariable("fileName") String fileName) throws IOException {
        return Files.readAllBytes(Paths.get(USER_FOLDER + userAccountName + FORWARD_SLASH + fileName));
    }

    @GetMapping(path = "/image/profile/{userAccountName}", produces = IMAGE_JPEG_VALUE)
    public byte[] getTempProfileImage(@PathVariable("userAccountName") String userAccountName) throws IOException {
        URL url = new URL(TEMP_PROFILE_IMAGE_BASE_URL + userAccountName);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = url.openStream()) {
            int bytesRead;
            byte[] chunk = new byte[1024];
            while ((bytesRead = inputStream.read(chunk)) > 0) {
                byteArrayOutputStream.write(chunk, 0, bytesRead);
            }
        }
        return byteArrayOutputStream.toByteArray();
    }

    private ResponseEntity<HttpResponseTemplate> customResponse(HttpStatus httpStatus, String message) {
        HttpResponseTemplate body = new HttpResponseTemplate(
                httpStatus.value(),
                httpStatus,
                httpStatus.getReasonPhrase().toUpperCase(),
                message.toUpperCase());

        return new ResponseEntity<>(body, httpStatus);
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
