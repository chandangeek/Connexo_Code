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

    public TaskContentInfos() {
    }

    public TaskContentInfos(JSONObject obj) {
        addAll(obj);
    }

    void addAll(JSONObject obj) {
        JSONArray contentProperties = null;
        JSONObject content = null;
        JSONObject outputContent = null;
        try {
            status = obj.getString("taskStatus");
            contentProperties = obj.getJSONArray("fields");
            content = obj.getJSONObject("content");
            outputContent = obj.getJSONObject("outContent");
            if(outputContent != null){
                setOutputContent(outputContent);
            }
        } catch (JSONException e) {
        }

        if (contentProperties != null) {
            for(int i = 0; i < contentProperties.length(); i++) {
                try {
                    JSONObject prop = contentProperties.getJSONObject(i);
                    TaskContentInfo result = new TaskContentInfo(prop, content, outputContent, status);
                    properties.add(result);
                } catch (JSONException e) {
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
