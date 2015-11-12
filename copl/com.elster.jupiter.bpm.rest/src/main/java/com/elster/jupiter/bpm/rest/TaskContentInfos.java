package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskContentInfos {

    public String status;

    public List<TaskContentInfo> properties = new ArrayList<>();

    public TaskContentInfos() {
    }

    public TaskContentInfos(JSONObject obj) {
        addAll(obj);
    }

    void addAll(JSONObject obj) {
        JSONArray contentProperties = null;
        JSONObject content = null;
        try {
            status = obj.getString("taskStatus");
            contentProperties = obj.getJSONArray("fields");
            content = obj.getJSONObject("content");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (contentProperties != null) {
            for(int i = 0; i < contentProperties.length(); i++) {
                try {
                    JSONObject prop = contentProperties.getJSONObject(i);
                    TaskContentInfo result = new TaskContentInfo(prop, content);
                    properties.add(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
