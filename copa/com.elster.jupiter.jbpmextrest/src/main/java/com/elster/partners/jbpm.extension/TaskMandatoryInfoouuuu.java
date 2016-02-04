package com.elster.partners.jbpm.extension;

import java.util.ArrayList;
import java.util.List;

public class TaskMandatoryInfoouuuu {

    public String name;
    public String processName;
    public String version;
    public List<Long> taskIds = new ArrayList<>();
    public long count;
    public boolean hasMandatory;
    public ConnexoForm form;

    public TaskMandatoryInfoouuuu(String name, String processName, String version, List<Long> taskIds, boolean hasMandatory, ConnexoForm form){
        this.name = name;
        this.processName = processName;
        this.version = version;
        this.taskIds = taskIds;
        this.hasMandatory = hasMandatory;
        this.form = form;
        this.count = taskIds.size();
    }

    public TaskMandatoryInfoouuuu(){

    }
}
