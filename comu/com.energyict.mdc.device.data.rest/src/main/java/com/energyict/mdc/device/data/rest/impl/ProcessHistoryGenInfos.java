package com.energyict.mdc.device.data.rest.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessHistoryGenInfos {
    public int total;
    public List<ProcessHistoryGenInfo> processHistories = new ArrayList<>();
    public List<String> errors;

    public ProcessHistoryGenInfos() {
    }

    public ProcessHistoryGenInfos(List<ProcessHistoryGenInfo> processHistories) {
        this();
        this.processHistories = processHistories;
        total = processHistories.size();
    }

    public ProcessHistoryGenInfos(List<ProcessHistoryGenInfo> processHistories, List<String> errors) {
        this(processHistories);
        this.errors = errors;
    }

    public ProcessHistoryGenInfos(JSONArray processInstances) {
        this();
        addAll(processInstances);
    }

    void addAll(JSONArray processInstances) {
        if (processInstances != null) {
            for(int i = 0; i < processInstances.length(); i++) {
                try {
                    JSONObject process = processInstances.getJSONObject(i);
                    ProcessHistoryGenInfo result = new ProcessHistoryGenInfo(process);
                    System.out.println("Add to processHistories="+result);
                    processHistories.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public String getValue(int index){
        return processHistories.get(index).getValue();
    }

    public String getVariableId(int index){
        return processHistories.get(index).getVariableId();
    }

    public void setObjectName(int index,  String nameToSet){
        processHistories.get(index).setObjectName(nameToSet);
    }

    public void setCorrDeviceName(int index,  String nameToSet){
        processHistories.get(index).setCorrDeviceName(nameToSet);
    }

    public void setIssueType(int index,  String typeToSet){
        processHistories.get(index).setIssueType(typeToSet);
    }

    public String getObjectName(int index){
        return processHistories.get(index).getObjectName();
    }

    public String getCorrDeviceName(int index){
        return processHistories.get(index).getCorrDeviceName();
    }
}
