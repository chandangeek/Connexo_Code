/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.impl;

import com.elster.jupiter.users.LdapUser;


public final class LdapUserImpl implements LdapUser {

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

    @Override
    public boolean equals(Object obj) {
        return obj instanceof LdapUserImpl && getUserName().equals(((LdapUserImpl) obj).getUserName());
    }

    @Override
    public int hashCode() {
        return getUserName().hashCode();
    }
}
