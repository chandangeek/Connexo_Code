/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.users.Group;


public class UserRolesInfo {

    public long id;
    public String name;

    public UserRolesInfo(){

    }

    public UserRolesInfo(Group group){
        this.id = group.getId();
        this.name = group.getName();
    }
}
