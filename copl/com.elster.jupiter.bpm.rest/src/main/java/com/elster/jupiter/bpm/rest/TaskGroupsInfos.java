/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TaskGroupsInfos {

    public List<TaskGroupsInfo> taskGroups = new ArrayList<>();

    public TaskGroupsInfos(){

    }

    public TaskGroupsInfos(JSONArray jsonArray){
        addAll(jsonArray);
    }

    private void addAll(JSONArray jsonArray){
        if(jsonArray != null){
            for(int i = 0; i < jsonArray.length(); i++){
                try {
                    JSONObject taskGroup = jsonArray.getJSONObject(i);
                    TaskGroupsInfo result = new TaskGroupsInfo(taskGroup);
                    taskGroups.add(result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }
    }

}
