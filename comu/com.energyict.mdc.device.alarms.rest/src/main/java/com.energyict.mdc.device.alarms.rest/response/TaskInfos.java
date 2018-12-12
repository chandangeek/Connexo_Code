/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.alarms.rest.response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskInfos {
    public int total;

    public List<TaskInfo> tasks = new ArrayList<>();

    public TaskInfos() {
    }

    public TaskInfos(JSONArray tasks) {
        addAll(tasks);
    }

    void addAll(JSONArray taskList) {
        if (taskList != null) {
            for(int i = 0; i < taskList.length(); i++) {
                try {
                    JSONObject task = taskList.getJSONObject(i);
                    TaskInfo result = new TaskInfo(task);
                    tasks.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
