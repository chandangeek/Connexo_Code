package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class TaskContentInfos {

    public String status;
    public String action;
    public String id;
    public BusinessObject businessObject;
    public String deploymentId;
    public List<TaskContentInfo> properties = new ArrayList<>();
    public Map<String, Object> outputContent = new HashMap<>();
    public String versionDB;
    public String processName;
    public String processVersion;

    public TaskContentInfos() {
    }

    public TaskContentInfos(JSONObject obj) throws JSONException {
        addAll(obj);
    }

    private void addAll(JSONObject obj) throws JSONException {
        status = obj.getString("taskStatus");
        JSONArray contentProperties = obj.getJSONArray("fields");
        JSONObject content = obj.optJSONObject("content");
        JSONObject outputContent = obj.optJSONObject("outContent");
        if (outputContent != null) {
            setOutputContent(outputContent);
        }
        if (contentProperties != null) {
            for(int i = 0; i < contentProperties.length(); i++) {
                JSONObject prop = contentProperties.getJSONObject(i);
                TaskContentInfo result = new TaskContentInfo(prop, content, outputContent, status);
                if(result.isVisible){
                    properties.add(result);
                }
            }
        }
    }

    private void setOutputContent(JSONObject outputContent){
        Iterator<?> keys = outputContent.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            try {
                this.outputContent.put(key, outputContent.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}