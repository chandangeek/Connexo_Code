/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.users.Group;

import java.util.List;
import java.util.stream.Collectors;

public class ProcessesPrivilegesInfo {

    public String id;
    public String applicationName;
    public String name;
    public List<UserRolesInfo> userRoles;

    public ProcessesPrivilegesInfo(){

    }

    public ProcessesPrivilegesInfo(String id, String name, String applicationName, List<Group> groups){
        this.id = id;
        this.applicationName = applicationName;
        this.name = name;
        userRoles = groups.stream()
                .filter(g -> g.hasPrivilege(applicationName, id))
                .map(s -> new UserRolesInfo(s))
                .collect(Collectors.toList());
    }


}