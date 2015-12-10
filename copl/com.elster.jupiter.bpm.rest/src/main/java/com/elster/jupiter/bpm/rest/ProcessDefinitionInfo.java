package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.BpmProcessDeviceState;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.users.Group;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

public class ProcessDefinitionInfo {
    public String name;
    public String id;
    public String version;
    public String active;
    public String associatedTo;
    public String deploymentId;
    public String activeDisplay;
    public List<ProcessesPrivilegesInfo> privileges;
    public List<DeviceLifeCycleStateSummaryInfo> deviceStates;

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
        this.associatedTo = bpmProcessDefinition.getAssociation();
        this.active = bpmProcessDefinition.getStatus();
    }

    public ProcessDefinitionInfo(BpmProcessDefinition bpmProcessDefinition, List<Group> groups){
        this.version = bpmProcessDefinition.getVersion();
        this.name = bpmProcessDefinition.getProcessName();
        this.associatedTo = bpmProcessDefinition.getAssociation();
        this.active = bpmProcessDefinition.getStatus();
        privileges = bpmProcessDefinition.getPrivileges().stream()
                .map(s -> new ProcessesPrivilegesInfo(s.getPrivilegeName(), Privileges.getDescriptionForKey(s.getPrivilegeName()), s.getApplication(), groups))
                .collect(Collectors.toList());
        deviceStates = bpmProcessDefinition.getProcessDeviceStates().stream()
                .map(s-> new DeviceLifeCycleStateSummaryInfo(s.getDeviceLifeCycleId(), s.getDeviceStateId(), s.getName(), s.getDeviceState()))
                .sorted((s1, s2) -> s1.deviceState.toLowerCase().compareTo(s2.deviceState.toLowerCase()))
                .sorted((s1, s2) -> s1.name.toLowerCase().compareTo(s2.name.toLowerCase()))
                .collect(Collectors.toList());
    }

}
