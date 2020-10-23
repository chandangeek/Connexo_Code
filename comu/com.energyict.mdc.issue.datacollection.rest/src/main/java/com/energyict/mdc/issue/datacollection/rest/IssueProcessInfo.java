/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest;


import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class IssueProcessInfo {

    public String processId;
    public String name;
    public String startDate;
    public String status;
    public String startedBy;
    public String associatedTo;
    public String version;
    public List<TaskInfo> openTasks;

    public IssueProcessInfo(){

    }

    public IssueProcessInfo(JSONObject jsonObject) throws JSONException {
            this.name = jsonObject.isNull("name") ? "" : jsonObject.getString("name") ;
            this.startDate = jsonObject.isNull("startDate") ? "" : jsonObject.getString("startDate") ;
            this.version = jsonObject.isNull("version") ? "" : jsonObject.getString("version") ;
            this.startedBy = jsonObject.isNull("startedBy") ? "" : jsonObject.getString("startedBy") ;
            this.processId = jsonObject.getString("processId").equals("-1") ? "" : jsonObject.getString("processId");
            this.status = jsonObject.getString("status").equals("-1") ? "" : jsonObject.getString("status");
            TaskInfos taskInfos = new TaskInfos(jsonObject.getJSONArray("openTasks"));
            this.openTasks = taskInfos.tasks;
    }
}
