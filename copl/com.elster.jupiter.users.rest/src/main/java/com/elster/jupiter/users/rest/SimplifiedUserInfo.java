/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;


import com.elster.jupiter.users.User;

public class SimplifiedUserInfo {

    public long id;
    public String name;
    public String description;
    public Boolean active;
    public String domain;

    public SimplifiedUserInfo(){

    }

    public SimplifiedUserInfo(User user){
        id = user.getId();
        name = user.getName();
        description = user.getDescription();
        active = user.getStatus();
        domain = user.getDomain();
    }
}
