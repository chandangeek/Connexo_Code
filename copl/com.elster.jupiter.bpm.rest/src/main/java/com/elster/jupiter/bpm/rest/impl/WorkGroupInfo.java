/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest.impl;

import com.elster.jupiter.users.WorkGroup;


public class WorkGroupInfo {

    public String name;
    public long id;

    public WorkGroupInfo(){
    }

    public WorkGroupInfo(String name){
        this.id = -1;
        this.name = name;
    }

    public WorkGroupInfo(WorkGroup workGroup){
        this.name = workGroup.getName();
        this.id = workGroup.getId();
    }

}
