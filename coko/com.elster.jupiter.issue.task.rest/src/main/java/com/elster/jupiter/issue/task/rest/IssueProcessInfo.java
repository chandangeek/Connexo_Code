/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.task.rest;


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
            this.name = jsonObject.isNull("processName") ? "" : jsonObject.getString("processName") ;
            this.startDate = jsonObject.isNull("startDate") ? "" : jsonObject.getString("startDate") ;
            this.version = jsonObject.isNull("processVersion") ? "" : jsonObject.getString("processVersion") ;
            this.startedBy = jsonObject.isNull("userIdentity") ? "" : jsonObject.getString("userIdentity") ;
            this.processId = jsonObject.getString("processInstanceId").equals("-1") ? "" : jsonObject.getString("processInstanceId");
            this.status = jsonObject.getString("status").equals("-1") ? "" : jsonObject.getString("status");
            TaskInfos taskInfos = new TaskInfos(jsonObject.getJSONArray("tasks"));
            this.openTasks = taskInfos.tasks;
    }
}
