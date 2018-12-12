/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessInstanceInfos {

    public int total;

    public List<ProcessInstanceInfo> processes = new ArrayList<>();

    public ProcessInstanceInfos() {
    }

    public ProcessInstanceInfos(JSONArray processInstances, String currentUser) {
        addAll(processInstances, currentUser);
    }

    public ProcessInstanceInfos(List<ProcessInstanceInfo> processInstances) {
        addAll(processInstances);
    }

    void addAll(JSONArray processInstances, String currentUser) {
        if (processInstances != null) {
            for (int i = 0; i < processInstances.length(); i++) {
                try {
                    JSONObject process = processInstances.getJSONObject(i);
                    ProcessInstanceInfo result = new ProcessInstanceInfo(process, currentUser);
                    processes.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    void addAll(List<ProcessInstanceInfo> processInstances) {
        if (processInstances != null) {
            for (int i = 0; i < processInstances.size(); i++) {
                processes.add(processInstances.get(i));
                total++;
            }
        }
    }
}
