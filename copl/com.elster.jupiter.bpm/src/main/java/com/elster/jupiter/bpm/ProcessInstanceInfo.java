/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ProcessInstanceInfo {

    public String processId;
    public String name;
    public String startDate;
    public String status;
    public String startedBy;
    public String associatedTo;
    public String version;
    public List<UserTaskInfo> openTasks;

    public ProcessInstanceInfo() {
    }

    public ProcessInstanceInfo(JSONObject jsonObject, String currentUser) {
        this();
        try {
            this.name = jsonObject.isNull("processName") ? "" : jsonObject.getString("processName");
            this.startDate = jsonObject.isNull("startDate") ? "" : jsonObject.getString("startDate");
            this.version = jsonObject.isNull("processVersion") ? "" : jsonObject.getString("processVersion");
            this.startedBy = jsonObject.isNull("userIdentity") ? "" : jsonObject.getString("userIdentity");
            this.processId = "-1".equals(jsonObject.getString("processInstanceId")) ? "" : jsonObject.getString("processInstanceId");
            this.status = "-1".equals(jsonObject.getString("status")) ? "" : jsonObject.getString("status");
            UserTaskInfos taskInfos = new UserTaskInfos(jsonObject.getJSONArray("tasks"), currentUser);
            this.openTasks = taskInfos.tasks;
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

}