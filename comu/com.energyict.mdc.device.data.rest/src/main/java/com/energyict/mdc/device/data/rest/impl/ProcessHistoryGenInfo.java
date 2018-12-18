package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.bpm.UserTaskInfo;
import com.elster.jupiter.bpm.UserTaskInfos;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProcessHistoryGenInfo {

    public String processId;
    public String name;
    public String startDate;
    public String endDate;
    public String status;
    public String startedBy;
    public String associatedTo;
    public String duration;
    public String version;
    public String value;
    public String variableId;
    public String objectName;
    public String corrDeviceName;
    public String issueType;

    public List<UserTaskInfo> openTasks;

    public ProcessHistoryGenInfo() {
    }

    public ProcessHistoryGenInfo(JSONObject jsonObject) {
        this();
        try {
            this.name = jsonObject.isNull("processName") ? "" : jsonObject.getString("processName") ;
            this.duration = jsonObject.isNull("duration") ? "" : jsonObject.getString("duration") ;
            this.startDate = jsonObject.isNull("startDate") ? "" : jsonObject.getString("startDate") ;
            this.endDate = jsonObject.isNull("endDate") ? "" : jsonObject.getString("endDate") ;
            this.version = jsonObject.isNull("processVersion") ? "" : jsonObject.getString("processVersion") ;
            this.startedBy = jsonObject.isNull("userIdentity") ? "" : jsonObject.getString("userIdentity") ;
            this.processId = "-1".equals(jsonObject.getString("processInstanceId")) ? "" : jsonObject.getString("processInstanceId");
            this.status = "-1".equals(jsonObject.getString("status")) ? "" : jsonObject.getString("status");
            this.value = jsonObject.isNull("value") ? "" : jsonObject.getString("value");
            this.variableId = jsonObject.isNull("variableId") ? "" : jsonObject.getString("variableId");
            this.objectName = "";
            this.corrDeviceName = "";
            this.issueType = "";
            UserTaskInfos taskInfos = new UserTaskInfos(jsonObject.getJSONArray("tasks"), "");
            this.openTasks = taskInfos.tasks;
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getValue(){
        return this.value;
    }

    public String getVariableId(){
        return this.variableId;
    }

    public void setObjectName(String objectName){
        this.objectName = objectName;
    }

    public void setIssueType(String typeToSet){
        this.issueType = typeToSet;
    }

    public void setCorrDeviceName(String deviceName){
        this.corrDeviceName = deviceName;
    }

    public String getObjectName(){
        return this.objectName;
    }

    public String getCorrDeviceName(){
        return this.corrDeviceName;
    }
}
