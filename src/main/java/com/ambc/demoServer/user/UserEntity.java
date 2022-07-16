package com.ambc.demoServer.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// ==> table => db
@Entity
public class UserEntity implements Serializable {
    //id ==> primary key
    @Id
    // random nbr generated
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false, updatable = false)
    //primary key (sensible data != exposed)
    private Long id;
    //userId used to be displayed in the app
    private String userIdentifier;
    private String userFirstName;
    private String userLastName;
    private String userAccountName;
    private String userEmail;
    private String userPassword;
    private String userProfilePictureLink;
    private Date userLastConnection;
    private Date UserLastConnectionToDisplay;
    private Date userSignUpDate;
    //admin {create, delete, ...}, user {}, ..
    private String userRole;
    private String[] userPermissions;
    //
    private Boolean isUserConnected;
    private Boolean isUserNotBanned;
}
