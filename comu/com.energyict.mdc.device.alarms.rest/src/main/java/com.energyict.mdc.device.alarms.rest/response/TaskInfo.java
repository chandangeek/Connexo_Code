/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;

import org.json.JSONException;
import org.json.JSONObject;

public class TaskInfo {
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

    public TaskInfo() {
    }

    public TaskInfo(JSONObject jsonObject) throws JSONException {
        this.id = jsonObject.getString("id");
        this.priority = jsonObject.getString("priority");
        this.name = jsonObject.isNull("name") ? "" : jsonObject.getString("name");
        this.processName = jsonObject.isNull("processName") ? "" : jsonObject.getString("processName");
        this.deploymentId = jsonObject.isNull("deploymentId") ? "" : jsonObject.getString("deploymentId");
        this.dueDate = jsonObject.isNull("dueDate") ? "" : jsonObject.getString("dueDate");
        this.createdOn = jsonObject.isNull("createdOn") ? "" : jsonObject.getString("createdOn");
        this.status = jsonObject.isNull("status") ? "" : jsonObject.getString("status");
        this.actualOwner = jsonObject.isNull("actualOwner") ? "" : jsonObject.getString("actualOwner");
        this.processInstancesId = jsonObject.getString("processInstanceId").equals("-1") ? "" : jsonObject.getString("processInstanceId");
    }

}
