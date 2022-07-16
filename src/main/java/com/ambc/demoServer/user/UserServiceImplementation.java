package com.ambc.demoServer.user;

import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;
import com.ambc.demoServer.user.roles.Role;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

import static com.ambc.demoServer.user.constants.UserImplementationConstants.*;

@Service
//manage propagations whenever dealing one or two transactions
@Transactional
//name for the bean that implement that interfaces
@Qualifier("ourUserDetailsService")
public class UserServiceImplementation implements UserService, UserDetailsService {
    private final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImplementation(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String userAccountName) throws UsernameNotFoundException {
        UserEntity user = userRepository.findUserByUserAccountName(userAccountName);
        if (user == null) {
            LOGGER.error(NO_USER_FOUND_BY_USERNAME + userAccountName);
            throw new UsernameNotFoundException(NO_USER_FOUND_BY_USERNAME + userAccountName);
        } else {
            user.setUserLastConnectionToDisplay(user.getUserLastConnection());
            user.setUserLastConnection(new Date());
            userRepository.save(user);
            UserSecuredDetails userSecuredDetails = new UserSecuredDetails(user);
            LOGGER.info(FOUND_USER_BY_USER_ACCOUNT_NAME + userAccountName);
            return userSecuredDetails;
        }
    }

    @Override
    public UserEntity userRegister(String userFirstName, String userLastName, String userAccountName, String userEmail) throws ExistsUserAccountName, ExistsEmail, NotFoundUser {
        checkIfEmailAndEmailIsAvailable(StringUtils.EMPTY, userAccountName, userEmail);
        UserEntity newUser = new UserEntity();
        newUser.setUserIdentifier(generateUserId());
        String password = generatePassword();
        String encodedPassword = encodePassword(password);
        newUser.setUserFirstName(userFirstName);
        newUser.setUserLastName(userLastName);
        newUser.setUserAccountName(userAccountName);
        newUser.setUserEmail(userEmail);
        newUser.setUserSignUpDate(new Date());
        newUser.setUserPassword(encodedPassword);
        newUser.setIsUserConnected(true);
        newUser.setIsUserNotBanned(true);
        newUser.setUserRole(Role.ROLE_USER.name());
        newUser.setUserPermissions(Role.ROLE_USER.getAuthorities());
        newUser.setUserProfilePictureLink(getTemporaryProfileImgUrl());
        userRepository.save(newUser);
        LOGGER.info("New User Pass: " + password);
        return newUser;
    }

    private String getTemporaryProfileImgUrl() {
        return ServletUriComponentsBuilder.fromCurrentContextPath().path(DEFAULT_USER_IMAGE_PROFILE_PATH).toUriString();
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    private String generatePassword() {
        return RandomStringUtils.randomAlphanumeric(10);
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
}
