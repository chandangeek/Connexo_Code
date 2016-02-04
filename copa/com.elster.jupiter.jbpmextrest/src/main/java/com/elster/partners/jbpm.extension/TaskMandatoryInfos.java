package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.List;


public class TaskMandatoryInfos {

    public List<TaskMandatoryInfo> taskGroups = new ArrayList<>();

    public TaskMandatoryInfos(List<TaskMandatoryInfo> taskGroups){
        this.taskGroups = taskGroups;
    }

    public TaskMandatoryInfos(){

    }

}
