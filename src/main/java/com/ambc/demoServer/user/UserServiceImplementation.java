package com.ambc.demoServer.user;

import com.ambc.demoServer.email.EmailService;
import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.emailExceptions.NotFoundEmail;
import com.ambc.demoServer.login.LoginAttemptService;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;
import com.ambc.demoServer.user.roles.Role;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.mail.MessagingException;
import javax.transaction.Transactional;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static com.ambc.demoServer.user.constants.ProfilePictureFileConstants.*;
import static com.ambc.demoServer.user.constants.UserImplementationConstants.*;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.springframework.http.MediaType.*;

@Service
//manage propagations whenever dealing one or two transactions
@Transactional
//name for the bean that implement that interfaces
@Qualifier("ourUserDetailsService")
public class UserServiceImplementation implements UserService, UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final LoginAttemptService loginAttemptService;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, EmailService emailService, LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.loginAttemptService = loginAttemptService;
    }

    @Override
    public UserDetails loadUserByUsername(String userAccountName) throws UsernameNotFoundException {
        UserEntity user = userRepository.findUserByUserAccountName(userAccountName);
        if (user == null) {
            LOGGER.error(NO_USER_FOUND_BY_USERNAME + userAccountName);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + userAccountName);
        } else {
            //Check if account is not ban before returning the user
            checkLoginAttempts(user);
            user.setUserLastConnectionToDisplay(user.getUserLastConnection());
            user.setUserLastConnection(new Date());
            userRepository.save(user);
            UserSecuredDetails userSecuredDetails = new UserSecuredDetails(user);
            LOGGER.info(FOUND_USER_BY_USER_ACCOUNT_NAME + userAccountName);
            return userSecuredDetails;
        }
    }

    private void checkLoginAttempts(UserEntity user) {
        //!= ban : remove from cache || == ban :
        if (user.getIsUserNotBanned()) {
            user.setIsUserNotBanned(!loginAttemptService.reachMaxAttempts(user.getUserAccountName()));
        } else {
            loginAttemptService.removeUserFromCache(user.getUserAccountName());
        }
    }

    @Override
    public UserEntity userRegister(String userFirstName, String userLastName, String userAccountName, String userEmail) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, MessagingException {
        checkIfEmailAndEmailIsAvailable(StringUtils.EMPTY, userAccountName, userEmail);
        UserEntity newUser = new UserEntity();
        newUser.setUserIdentifier(generateUserId());
        String password = generatePassword();
        newUser.setUserFirstName(userFirstName);
        newUser.setUserLastName(userLastName);
        newUser.setUserAccountName(userAccountName);
        newUser.setUserEmail(userEmail);
        newUser.setUserSignUpDate(new Date());
        newUser.setUserPassword(encodePassword(password));
        newUser.setIsUserConnected(true);
        newUser.setIsUserNotBanned(true);
        newUser.setUserRole(Role.ROLE_USER.name());
        newUser.setUserPermissions(Role.ROLE_USER.getAuthorities());
        newUser.setUserProfilePictureLink(getTemporaryProfileImgUrl(userAccountName));
        userRepository.save(newUser);
        LOGGER.info("Password : " + password);
        emailService.sendNewPassWord(userFirstName, password, userEmail);
        return newUser;
    }

    @Override
    public UserEntity addNewUserEntity(String userFirstName, String userLastName, String userAccountName, String userEmail, String role, boolean isUserNotBanned, boolean isUserConnected, MultipartFile profileImage) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException, MessagingException {
        checkIfEmailAndEmailIsAvailable(StringUtils.EMPTY, userAccountName, userEmail);
        UserEntity newUser = new UserEntity();
        String password = generatePassword();
        newUser.setUserIdentifier(generateUserId());
        newUser.setUserFirstName(userFirstName);
        newUser.setUserLastName(userLastName);
        newUser.setUserSignUpDate(new Date());
        newUser.setUserAccountName(userAccountName);
        newUser.setUserEmail(userEmail);
        newUser.setUserPassword(encodePassword(password));
        newUser.setIsUserConnected(isUserConnected);
        newUser.setIsUserNotBanned(isUserNotBanned);
        newUser.setUserRole(getRoleName(role).name());
        newUser.setUserPermissions(getRoleName(role).getAuthorities());
        newUser.setUserProfilePictureLink(getTemporaryProfileImgUrl(userAccountName));
        userRepository.save(newUser);
        saveProfileImage(newUser, profileImage);
        emailService.sendNewPassWord(newUser.getUserFirstName(), password, newUser.getUserEmail());
        LOGGER.info("New user password: " + password);
        return newUser;
    }

    @Override
    public UserEntity updateUserEntity(String currentUserAccountName, String newUserFirstName, String newUserLastName, String newUserAccountName, String newUserEmail, String role, boolean isUserNotBanned, boolean isUserConnected, MultipartFile profileImage) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException {
        UserEntity currentUser = checkIfEmailAndEmailIsAvailable(currentUserAccountName, newUserAccountName, newUserEmail);
        currentUser.setUserFirstName(newUserFirstName);
        currentUser.setUserLastName(newUserLastName);
        currentUser.setUserAccountName(newUserAccountName);
        currentUser.setUserEmail(newUserEmail);
        currentUser.setIsUserConnected(isUserConnected);
        currentUser.setIsUserNotBanned(isUserNotBanned);
        currentUser.setUserRole(getRoleName(role).name());
        currentUser.setUserPermissions(getRoleName(role).getAuthorities());
        userRepository.save(currentUser);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }


    @Override
    public void deleteUserEntity(String userAccountName) throws IOException {
        UserEntity user = userRepository.findUserByUserAccountName(userAccountName);
        Path userFolder = Paths.get(USER_FOLDER + user.getUserAccountName()).toAbsolutePath().normalize();
        FileUtils.deleteDirectory(new File(userFolder.toString()));
        userRepository.deleteById(user.getId());
    }

    @Override
    public void resetUserPassword(String userEmail) throws MessagingException, NotFoundEmail {
        UserEntity currentUser = userRepository.findUserByUserEmail(userEmail);

        if (currentUser == null) {
            throw new NotFoundEmail(NO_USER_FOUND_BY_EMAIL + userEmail);
        }

        String newPassword = generatePassword();
        currentUser.setUserPassword(encodePassword(newPassword));
        userRepository.save(currentUser);
        emailService.sendNewPassWord(currentUser.getUserFirstName(), newPassword, currentUser.getUserEmail());
    }

    private String getTemporaryProfileImgUrl(String userAccountName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PATH + userAccountName).toUriString();
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
    }


    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generateUserId() {
        return RandomStringUtils.randomNumeric(10);
    }

    private UserEntity checkIfEmailAndEmailIsAvailable(String currentUserAccountName, String newUserAccountName, String newUserEmail) throws ExistsUserAccountName, ExistsEmail, NotFoundUser {
        UserEntity userByNewUserAccountName = findUserEntityByUserAccountName(newUserAccountName);
        UserEntity userByNewUserEmail = findByUserEmail(newUserEmail);

        if (StringUtils.isNotBlank(currentUserAccountName)) {
            UserEntity currentUser = findUserEntityByUserAccountName(currentUserAccountName);
            if (currentUser == null) {
                throw new NotFoundUser(NO_USER_FOUND_BY_USERNAME + currentUserAccountName);
            }
            if (userByNewUserAccountName != null && !currentUser.getId().equals(userByNewUserAccountName.getId())) {
                throw new ExistsUserAccountName(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewUserEmail != null && !currentUser.getId().equals(userByNewUserEmail.getId())) {
                throw new ExistsEmail(EMAIL_ALREADY_EXISTS);
            }
            return currentUser;
        } else {
            if (userByNewUserAccountName != null) {
                throw new ExistsUserAccountName(USERNAME_ALREADY_EXISTS);
            }
            if (userByNewUserEmail != null) {
                throw new ExistsEmail(EMAIL_ALREADY_EXISTS);
            }
            return null;
        }
    }

    @Override
    public List<UserEntity> getAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public UserEntity findUserEntityByUserAccountName(String userAccountName) {
        return userRepository.findUserByUserAccountName(userAccountName);
    }

    @Override
    public UserEntity findByUserEmail(String userEmail) {
        return userRepository.findUserByUserEmail(userEmail);
    }

    @Override
    public UserEntity updateUserProfileImage(String userAccountName, MultipartFile profileImage) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException {
        UserEntity currentUser = checkIfEmailAndEmailIsAvailable(userAccountName, null, null);
        saveProfileImage(currentUser, profileImage);
        return currentUser;
    }

    private void saveProfileImage(UserEntity newUser, MultipartFile profileImage) throws IOException {
        if (profileImage != null) {
            if (!Arrays.asList(IMAGE_JPEG_VALUE, IMAGE_PNG_VALUE, IMAGE_GIF_VALUE).contains(profileImage.getContentType())) {
                throw new IOException(profileImage.getOriginalFilename() + " n'est pas au bon format. Seuls les .jpeg, .png et .gif sont accepter");
            }
            Path userFolder = Paths.get(USER_FOLDER + newUser.getUserAccountName()).toAbsolutePath().normalize();
            if (!Files.exists(userFolder)) {
                Files.createDirectories(userFolder);
                LOGGER.info(DIRECTORY_CREATED + userFolder);
            }
            Files.deleteIfExists(Paths.get(userFolder + newUser.getUserAccountName() + DOT + JPG_EXTENSION));
            Files.copy(profileImage.getInputStream(), userFolder.resolve(newUser.getUserAccountName() + DOT + JPG_EXTENSION), REPLACE_EXISTING);
            newUser.setUserProfilePictureLink(setProfilePictureUrl(newUser.getUserAccountName()));
            userRepository.save(newUser);
            LOGGER.info(FILE_SAVED_IN_FILE_SYSTEM + profileImage.getOriginalFilename());
        }
    }

    private String setProfilePictureUrl(String userAccountName) {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(USER_IMAGE_PATH + userAccountName + FORWARD_SLASH + userAccountName + DOT + JPG_EXTENSION).toUriString();
    }

    private Role getRoleName(String role) {
        return Role.valueOf(role.toUpperCase());
    }
}
