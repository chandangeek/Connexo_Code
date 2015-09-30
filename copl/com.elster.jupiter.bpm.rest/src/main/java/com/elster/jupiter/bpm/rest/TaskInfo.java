package com.elster.jupiter.bpm.rest;


import org.json.JSONException;
import org.json.JSONObject;

public class TaskInfo {
    public String status;
    public String name;
    public String createdOn;
    public String dueDate;
    public long processInstancesId;
    public String actualOwner;
    public TaskInfo() {
    }

    public TaskInfo(JSONObject jsonObject){
        try{
            this.processInstancesId = jsonObject.getLong("processInstanceId");
            this.name = jsonObject.getString("name");
            this.status = jsonObject.getString("status");
            this.createdOn = jsonObject.getString("createdOn");
            this.dueDate = jsonObject.getString("expirationTime");
            this.actualOwner = jsonObject.getString("actualOwnerId");
        } catch (JSONException e) {
        throw new RuntimeException(e);
    }
    }
}
