/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.nls.Thesaurus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceNodeInfos {

    public String processInstanceStatus;
    public List<ProcessInstanceNodeInfo> processInstanceNodes = new ArrayList<ProcessInstanceNodeInfo>();

    public ProcessInstanceNodeInfos(){

    }

    public ProcessInstanceNodeInfos(JSONObject jsonObject, Thesaurus thesaurus){
        JSONArray nodes = null;
        JSONArray variables = null;
        try {
            nodes = jsonObject.getJSONArray("processInstanceNodes");
            variables = jsonObject.getJSONArray("processInstanceVariables");
            this.processInstanceStatus = jsonObject.getString("processInstanceStatus");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addAllNodes(nodes, variables, thesaurus);
    }

    private void addAllNodes(JSONArray nodes,JSONArray variables, Thesaurus thesaurus) {
        if (nodes != null) {
            for(int i = 0; i < nodes.length(); i++) {
                try {
                    JSONObject node = nodes.getJSONObject(i);
                    ProcessInstanceNodeInfo result = new ProcessInstanceNodeInfo(node, variables, thesaurus);
                    processInstanceNodes.add(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
