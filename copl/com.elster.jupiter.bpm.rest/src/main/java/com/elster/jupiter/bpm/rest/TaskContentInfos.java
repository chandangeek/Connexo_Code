/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        Optional<JSONArray> fields = obj.isNull("fields") ? Optional.empty() : Optional.of(obj.getJSONArray("fields"));
        Optional<JSONObject> content = obj.isNull("content") ? Optional.empty() : Optional.of(obj.getJSONObject("content"));
        Optional<JSONObject> outContent = obj.isNull("outContent") ? Optional.empty() : Optional.of(obj.getJSONObject("outContent"));
        if (outContent.isPresent()) {
            setOutputContent(outContent.get());
        }
        if (fields.isPresent()) {
            for (int i = 0; i < fields.get().length(); i++) {
                JSONObject field = fields.get().getJSONObject(i);
                TaskContentInfo result = new TaskContentInfo(field, content, outContent, status);
                if(result.isVisible){
                    properties.add(result);
                }
            }
        }
    }

    private void setOutputContent(JSONObject outContent) {
        Iterator<?> keys = outContent.keys();
        while( keys.hasNext() ) {
            String key = (String)keys.next();
            try {
                this.outputContent.put(key, outContent.get(key));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}