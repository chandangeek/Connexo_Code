package com.elster.jupiter.bpm.rest;


import java.util.Map;

public class TaskOutputContentInfo {

    public Map<String, Object> outputTaskContent;

    public TaskOutputContentInfo(){
    }

    public TaskOutputContentInfo(Map<String, Object> outputTaskContent){
        this.outputTaskContent = outputTaskContent;
    }
}
