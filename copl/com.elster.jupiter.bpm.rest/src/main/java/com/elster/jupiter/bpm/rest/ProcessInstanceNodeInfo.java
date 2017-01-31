/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.util.json.JsonDeserializeException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceNodeInfo {

    public String name;
    public String type;
    public String status;
    public String logDate;
    public String nodeInstanceId;
    public List<ProcessInstanceVariableInfo> processInstanceVariables =  new ArrayList<ProcessInstanceVariableInfo>();

    public ProcessInstanceNodeInfo(JSONObject jsonObject, JSONArray variables, Thesaurus thesaurus){
        try {
            this.name = jsonObject.getString("nodeName");
            this.type = thesaurus.getString(jsonObject.getString("nodeType"), jsonObject.getString("nodeType"));
            this.status = jsonObject.getString("type");
            this.logDate = jsonObject.getString("logDate");
            this.nodeInstanceId = jsonObject.getString("nodeInstanceId");
            if (variables != null) {
                for(int i = 0; i < variables.length(); i++) {
                    JSONObject variable = variables.getJSONObject(i);
                    if(variable.getString("nodeInstanceId").equals(nodeInstanceId)) {
                        ProcessInstanceVariableInfo result = new ProcessInstanceVariableInfo(variable);
                        processInstanceVariables.add(result);
                    }
                }
            }
        } catch (JSONException e) {
            throw new JsonDeserializeException(e, jsonObject.toString(), ProcessInstanceNodeInfo.class);
        }
    }

}
