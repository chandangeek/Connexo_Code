package com.energyict.mdc.device.data.rest.impl;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessInfosForFilter {

    public int total;

    public List<ProcessInfoForFilter> processes = new ArrayList<>();

    public ProcessInfosForFilter() {
    }

    public ProcessInfosForFilter(JSONArray processes) {
        addAll(processes);
    }

    private void addAll(JSONArray processList) {
        if (processList != null) {
            for(int i = 0; i < processList.length(); i++) {
                try {
                    JSONObject task = processList.getJSONObject(i);
                    ProcessInfoForFilter result = new ProcessInfoForFilter(task);
                    processes.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
