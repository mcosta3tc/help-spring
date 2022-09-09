package com.ambc.demoServer.user;

import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.emailExceptions.NotFoundEmail;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;
import org.springframework.web.multipart.MultipartFile;

import javax.mail.MessagingException;
import java.io.IOException;
import java.util.List;

public interface UserService {

    UserEntity userRegister(String userFirstName, String userLastName, String userAccountName, String userEmail) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, MessagingException;

    List<UserEntity> getAllUsers();

    UserEntity findUserEntityByUserAccountName(String userAccountName);

    UserEntity findByUserEmail(String userEmail);

    UserEntity addNewUserEntity(String userFirstName, String userLastName, String userAccountName, String userEmail, String role, boolean isUserNotBanned, boolean isUserConnected, MultipartFile profileImage) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException, MessagingException;

    UserEntity updateUserEntity(String currentUserAccountName, String newUserFirstName, String newUserLastName, String newUserAccountName, String newUserEmail, String role, boolean isUserNotBanned, boolean isUserConnected, MultipartFile profileImage) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException;

    void deleteUserEntity(String userAccountName) throws IOException;

    void resetUserPassword(String userEmail) throws MessagingException, NotFoundEmail;

    UserEntity updateUserProfileImage(String userAccountName, MultipartFile profileImage) throws ExistsUserAccountName, ExistsEmail, NotFoundUser, IOException;
}
