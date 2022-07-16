package com.ambc.demoServer.user;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    UserEntity findUserByUserAccountName(String userAccountName);

    UserEntity findUserByUserEmail(String userEmail);
}
