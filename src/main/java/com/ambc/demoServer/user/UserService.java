package com.ambc.demoServer.user;

import com.ambc.demoServer.emailExceptions.ExistsEmail;
import com.ambc.demoServer.user.exceptions.ExistsUserAccountName;
import com.ambc.demoServer.user.exceptions.NotFoundUser;

import java.util.List;

public interface UserService {

    UserEntity userRegister(String userFirstName, String userLastName, String userAccountName, String userEmail) throws ExistsUserAccountName, ExistsEmail, NotFoundUser;

    List<UserEntity> getAllUsers();

    UserEntity findUserEntityByUserAccountName(String userAccountName);

    UserEntity findByUserEmail(String userEmail);
}
