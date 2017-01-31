/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import com.elster.jupiter.util.json.JsonDeserializeException;
import org.json.JSONException;
import org.json.JSONObject;

public class TaskBulkReportInfo {

    public long total;
    public long failed;

    public TaskBulkReportInfo(){

    }

    public TaskBulkReportInfo(JSONObject object){
        try {
            this.total = object.getLong("total");
            this.failed = object.getLong("failed");
        } catch (JSONException e) {
            throw new JsonDeserializeException(e, object.toString(), TaskBulkReportInfo.class);
        }
    }
}
