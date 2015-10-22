package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import org.json.JSONException;
import org.json.JSONObject;

public class ProcessDefinitionInfo {
    public String name;
    public String id;
    public String version;
    public boolean active;
    public String associated;
    public String deploymentId;

    public ProcessDefinitionInfo(){

    }

    public ProcessDefinitionInfo(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString("name");
            this.id = jsonObject.getString("id");
            this.version = jsonObject.getString("version");
            this.deploymentId = jsonObject.getString("deploymentId");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public ProcessDefinitionInfo(BpmProcessDefinition bpmProcessDefinition){
        this.version = bpmProcessDefinition.getVersion();
        this.name = bpmProcessDefinition.getProcessName();
        this.associated = bpmProcessDefinition.getAssociation();
        this.active = bpmProcessDefinition.getState();
    }

    public boolean getState(){
        return  active;
    }
}
