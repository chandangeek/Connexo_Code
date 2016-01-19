package com.elster.jupiter.bpm.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessInstanceNodeInfo {

    public String nodeName;
    public String nodeType;
    public String status;
    public String logDate;
    public String nodeInstanceId;

    public ProcessInstanceNodeInfo(JSONObject jsonObject){
        try {
            this.nodeName = jsonObject.getString("nodeName");
            this.nodeType = jsonObject.getString("nodeType");
            this.status = jsonObject.getString("type");
            this.logDate = jsonObject.getString("logDate");
            this.nodeInstanceId = jsonObject.getString("nodeInstanceId");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
