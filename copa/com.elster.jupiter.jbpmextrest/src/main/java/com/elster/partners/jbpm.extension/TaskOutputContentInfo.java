package com.elster.partners.jbpm.extension;


import java.util.Map;

public class TaskOutputContentInfo {

    public Map<String, Object> outputTaskContent;

    public TaskOutputContentInfo(){
    }

    public TaskOutputContentInfo(Map<String, Object> outputTaskContent){
        this.outputTaskContent = outputTaskContent;
    }
}
