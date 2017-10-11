/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskGroupsInfo {

    public String name;
    public String processName;
    public String version;
    public List<Long> taskIds = new ArrayList<>();
    public long count;
    public boolean hasMandatory;
    public ConnexoForm tasksForm;
    public Map<String, Object> outputBindingContents;

    public TaskGroupsInfo(String name, String processName, String version, List<Long> taskIds, boolean hasMandatory, ConnexoForm tasksForm){
        this.name = name;
        this.processName = processName;
        this.version = version;
        this.taskIds = taskIds;
        this.hasMandatory = hasMandatory;
        this.tasksForm = tasksForm;
        this.count = taskIds.size();
    }

    public TaskGroupsInfo(){

    }
}
