/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.impl.UserImpl;

public class LdapUsersInfo {

    public String name;
    public boolean status;

    public LdapUsersInfo(){

    }

    public LdapUsersInfo(LdapUser ldapUser){
        name = ldapUser.getUserName();
        status = ldapUser.getStatus();
    }

    public LdapUsersInfo(User ldapUser){
        name = ldapUser.getName();
        status = ldapUser.getStatus();
    }

}
