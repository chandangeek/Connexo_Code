package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class RunningProcessInfos {

    public int total;

    public List<RunningProcessInfo> processes = new ArrayList<>();

    public RunningProcessInfos() {
    }

    public RunningProcessInfos(JSONArray processInstances) {
        addAll(processInstances);
    }

    void addAll(JSONArray processInstances) {
        if (processInstances != null) {
            for(int i = 0; i < processInstances.length(); i++) {
                try {
                    JSONObject process = processInstances.getJSONObject(i);
                    RunningProcessInfo result = new RunningProcessInfo(process);
                    processes.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }


}
