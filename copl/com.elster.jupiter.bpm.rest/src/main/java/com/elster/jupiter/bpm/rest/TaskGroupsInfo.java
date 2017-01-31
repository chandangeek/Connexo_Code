/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TaskGroupsInfo {

    public String name;
    public String processName;
    public String version;
    public List<Long> taskIds = new ArrayList<>();
    public long count = 0;
    public boolean hasMandatory;
    public TaskContentInfos tasksForm;
    public Map<String, Object> outputBindingContents;

    public TaskGroupsInfo() {
    }

    public TaskGroupsInfo(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString("name");
            this.processName = jsonObject.getString("processName");
            this.version = jsonObject.getString("version");
            this.hasMandatory = jsonObject.getBoolean("hasMandatory");
            JSONObject form = jsonObject.getJSONObject("tasksForm");
            tasksForm = new TaskContentInfos(form);
            JSONArray ids = jsonObject.getJSONArray("taskIds");
            for(int i = 0; i< ids.length(); i++){
                taskIds.add(ids.getLong(i));
            }
            this.count = taskIds.size();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}