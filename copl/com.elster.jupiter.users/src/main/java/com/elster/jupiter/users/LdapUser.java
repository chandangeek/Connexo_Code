/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users;


public interface LdapUser {

    void setUsername(String username);

    String getUserName();

    void setStatus(boolean status);

    boolean getStatus();
}
