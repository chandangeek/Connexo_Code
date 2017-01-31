/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.users.rest;


import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.users.WorkGroup;
import com.elster.jupiter.users.impl.UsersInWorkGroup;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WorkGroupInfo {

    public long id;
    public String name;
    public String description;
    public long version;
    public List<SimplifiedUserInfo> users = new ArrayList<>();

    public WorkGroupInfo() {
    }

    public WorkGroupInfo(WorkGroup workGroup){
        id = workGroup.getId();
        name = workGroup.getName();
        description = workGroup.getDescription();
        version = workGroup.getVersion();
        users.addAll(workGroup.getUsersInWorkGroup().stream().map(SimplifiedUserInfo::new).collect(Collectors.toList()));
    }

}
