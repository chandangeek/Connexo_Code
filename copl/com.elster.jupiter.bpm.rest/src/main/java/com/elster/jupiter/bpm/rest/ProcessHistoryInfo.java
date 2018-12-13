/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessHistoryInfo {

    public String processId;
    public String name;
    public String startDate;
    public String endDate;
    public String status;
    public String startedBy;
    public String associatedTo;
    public String duration;
    public String version;

    public ProcessHistoryInfo() {
    }

    public ProcessHistoryInfo(JSONObject jsonObject) {
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
        } catch (JSONException e) {
            throw new IllegalArgumentException(e);
        }
    }

}