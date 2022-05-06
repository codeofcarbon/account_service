package com.codeofcarbon.account.model;

public enum Action {
    CREATE_USER,                        // user has been successfully registered
    CHANGE_PASSWORD,                    // user has changed the password successfully
    ACCESS_DENIED,                      // user is trying to access a resource without access rights
    LOGIN_FAILED,                       // failed authentication
    GRANT_ROLE,                         // role is granted to a user
    REMOVE_ROLE,                        // role has been revoked
    DELETE_USER                        // administrator has deleted a user
}
