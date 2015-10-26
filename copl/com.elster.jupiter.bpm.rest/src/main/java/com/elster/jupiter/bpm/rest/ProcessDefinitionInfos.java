package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessDefinitionInfos {

    public int total;

    public List<ProcessDefinitionInfo> processes = new ArrayList<>();

    public ProcessDefinitionInfos() {
    }

    public ProcessDefinitionInfos(JSONArray processes) {
        addAll(processes);
    }

    void addAll(JSONArray processList) {
        if (processList != null) {
            for(int i = 0; i < processList.length(); i++) {
                try {
                    JSONObject task = processList.getJSONObject(i);
                    ProcessDefinitionInfo result = new ProcessDefinitionInfo(task);
                    processes.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<ProcessDefinitionInfo> getProcesses() {
        return processes;
    }
}
