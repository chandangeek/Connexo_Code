package com.elster.jupiter.users.impl;

import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.users.LdapUser;
import com.elster.jupiter.users.UserDirectory;

import javax.inject.Inject;


public class LdapUserImpl implements LdapUser {

    private String username;
    private boolean status;

    LdapUserImpl init(String username, boolean status){
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
    public void setStatus(boolean status) {
        this.status = status;
    }

    @Override
    public boolean getStatus() {
        return status;
    }
}
