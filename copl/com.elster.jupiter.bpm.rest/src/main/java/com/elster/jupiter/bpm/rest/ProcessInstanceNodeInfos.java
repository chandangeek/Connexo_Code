package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceNodeInfos {

    public String processInstanceStatus;
    public List<ProcessInstanceNodeInfo> processInstanceNodes = new ArrayList<ProcessInstanceNodeInfo>();
    public List<ProcessInstanceVariableInfo> processInstanceVariables = new ArrayList<ProcessInstanceVariableInfo>();

    public ProcessInstanceNodeInfos(){

    }

    public ProcessInstanceNodeInfos(JSONObject jsonObject){
        JSONArray nodes = null;
        JSONArray variables = null;
        try {
            nodes = jsonObject.getJSONArray("processInstanceNodes");
            variables = jsonObject.getJSONArray("processInstanceVariables");
            this.processInstanceStatus = jsonObject.getString("processInstanceStatus");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        addAllNodes(nodes);
        addAllVariables(variables);
    }

    private void addAllVariables(JSONArray variables) {
        if (variables != null) {
            for(int i = 0; i < variables.length(); i++) {
                try {
                    JSONObject variable = variables.getJSONObject(i);
                    ProcessInstanceVariableInfo result = new ProcessInstanceVariableInfo(variable);
                    processInstanceVariables.add(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void addAllNodes(JSONArray nodes) {
        if (nodes != null) {
            for(int i = 0; i < nodes.length(); i++) {
                try {
                    JSONObject node = nodes.getJSONObject(i);
                    ProcessInstanceNodeInfo result = new ProcessInstanceNodeInfo(node);
                    processInstanceNodes.add(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
