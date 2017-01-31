/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessInstanceVariableInfo {

    public String variableName;
    public String value;
    public String oldValue;
    public String logDate;
    public String nodeInstanceId;

    public ProcessInstanceVariableInfo(JSONObject jsonObject){
        try {
            this.variableName = jsonObject.getString("variableName");
            this.value = jsonObject.getString("value");
            this.oldValue = jsonObject.getString("oldValue");
            this.logDate = jsonObject.getString("logDate");
            this.nodeInstanceId = jsonObject.getString("nodeInstanceId");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
