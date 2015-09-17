package com.elster.jupiter.users.rest;

import com.elster.jupiter.users.LdapUser;

public class LdapUsersInfo {

    public String name;
    public String status;

    public LdapUsersInfo(){

    }

    public LdapUsersInfo(LdapUser ldapUser){
        name = ldapUser.getUserName();
        status = ldapUser.getStatus();
    }
}
