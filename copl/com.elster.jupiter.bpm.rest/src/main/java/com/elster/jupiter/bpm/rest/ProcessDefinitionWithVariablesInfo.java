/*
 * Copyright (c) 2018 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.bpm.security.Privileges;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.users.Group;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ProcessDefinitionWithVariablesInfo {

    public String name;
    public String processId;
    public String version;
    public String active;
    public String type;
    public String displayType;
    public String deploymentId;
    public String appKey;
    public List<ProcessesPrivilegesInfo> privileges;
    public List<PropertyInfo> properties = Collections.emptyList();
    public long versionDB;
    public List<ProcessVariable> variables; // CXO-8771

    public ProcessDefinitionWithVariablesInfo() {

    }

    public ProcessDefinitionWithVariablesInfo(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString("name");
            this.processId = jsonObject.getString("id");
            this.version = jsonObject.getString("version");
            this.deploymentId = jsonObject.getString("deploymentId");
            this.variables = new ArrayList<>();
            String flowProcessVariables = jsonObject.getString("variables");
            Map<String, String> processVarMap = new ObjectMapper().readValue(flowProcessVariables, Map.class);
            for (String name : processVarMap.keySet()) {
                String key = name.toString();
                String value = processVarMap.get(name).toString();
                this.variables.add(new ProcessVariable(key, value));
            }
        } catch (JSONException e) {
            throw new RuntimeException(e);
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    public ProcessDefinitionWithVariablesInfo(BpmProcessDefinition bpmProcessDefinition) {
        this.version = bpmProcessDefinition.getVersion();
        this.name = bpmProcessDefinition.getProcessName();
        this.type = bpmProcessDefinition.getAssociationProvider()
                .isPresent() ? bpmProcessDefinition.getAssociationProvider().get().getType() : "";
        this.displayType = bpmProcessDefinition.getAssociationProvider()
                .isPresent() ? bpmProcessDefinition.getAssociationProvider().get().getName() : "";
        this.active = bpmProcessDefinition.getStatus();
        this.appKey = bpmProcessDefinition.getAssociationProvider()
                .isPresent() ? bpmProcessDefinition.getAssociationProvider().get().getAppKey() : "";
        this.versionDB = bpmProcessDefinition.getVersionDB();
    }

    public ProcessDefinitionWithVariablesInfo(BpmProcessDefinition bpmProcessDefinition, List<Group> groups, Thesaurus thesaurus) {
        this.version = bpmProcessDefinition.getVersion();
        this.name = bpmProcessDefinition.getProcessName();
        this.type = bpmProcessDefinition.getAssociationProvider()
                .isPresent() ? bpmProcessDefinition.getAssociationProvider().get().getType() : "";
        this.displayType = bpmProcessDefinition.getAssociationProvider()
                .isPresent() ? bpmProcessDefinition.getAssociationProvider().get().getName() : "";
        this.appKey = bpmProcessDefinition.getAssociationProvider()
                .isPresent() ? bpmProcessDefinition.getAssociationProvider().get().getAppKey() : "";
        this.active = bpmProcessDefinition.getStatus();
        this.privileges = bpmProcessDefinition.getPrivileges().stream()
                .map(s -> new ProcessesPrivilegesInfo(s.getPrivilegeName(), Privileges.getDescriptionForKey(s.getPrivilegeName(), thesaurus), s.getApplication(), groups))
                .collect(Collectors.toList());
        this.versionDB = bpmProcessDefinition.getVersionDB();
    }

    public void setProperties(List<PropertyInfo> properties) {
        this.properties = properties;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
