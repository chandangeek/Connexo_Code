/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.List;


public class TaskGroupsInfos {

    public List<TaskGroupsInfo> taskGroups = new ArrayList<>();

    public TaskGroupsInfos(List<TaskGroupsInfo> taskGroups){
        this.taskGroups = taskGroups;
    }

    public TaskGroupsInfos(){

    }

}
