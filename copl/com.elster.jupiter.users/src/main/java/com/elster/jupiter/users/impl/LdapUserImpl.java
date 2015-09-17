package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.UserDirectory;

import javax.inject.Inject;


public class LdapUserImpl implements LdapUser {

    private String username;
    private String status;

    LdapUserImpl init(String username, String status){
        this.username = username;
        this.status = status;
        return this;
    }
    @Override
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String getUserName() {
        return username;
    }

    @Override
    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String getStatus() {
        return status;
    }
}
