package com.elster.jupiter.bpm;


import org.json.JSONException;
import org.json.JSONObject;

public class UserTaskInfo {
    public String id;
    public String priority;
    public String name;
    public String processName = "";
    public String deploymentId;
    public String status;
    public String dueDate;
    public String createdOn;
    public String actualOwner = "";
    public String processInstancesId;
    public String optLock;
    public boolean isAssignedToCurrentUser;

    public UserTaskInfo() {
    }

    public UserTaskInfo(JSONObject jsonObject, String currentUser) {
        try {
            this.id = jsonObject.getString("id");
            this.processInstancesId = jsonObject.getString("processInstanceId")
                    .equals("-1") ? "" : jsonObject.getString("processInstanceId");
            this.name = jsonObject.isNull("name") ? "" : jsonObject.getString("name");
            this.processName = jsonObject.isNull("processName") ? "" : jsonObject.getString("processName");
            this.deploymentId = jsonObject.isNull("deploymentId") ? "" : jsonObject.getString("deploymentId");
            this.dueDate = jsonObject.isNull("dueDate") ? "" : jsonObject.getString("dueDate");
            this.priority = jsonObject.getString("priority");
            this.status = jsonObject.isNull("status") ? "" : jsonObject.getString("status");
            this.createdOn = jsonObject.isNull("createdOn") ? "" : jsonObject.getString("createdOn");
            this.actualOwner = jsonObject.isNull("actualOwner") ? "" : jsonObject.getString("actualOwner");
            this.isAssignedToCurrentUser = (!currentUser.isEmpty() && this.actualOwner.equals(currentUser));
            this.optLock = jsonObject.getString("optLock").equals("-1") ? "" : jsonObject.getString("optLock");
        } catch (JSONException e) {
        }
    }

}
