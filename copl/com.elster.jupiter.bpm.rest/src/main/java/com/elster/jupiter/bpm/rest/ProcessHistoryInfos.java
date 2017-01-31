/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ProcessHistoryInfos {

    public int total;

    public List<ProcessHistoryInfo> processHistories = new ArrayList<>();

    public ProcessHistoryInfos() {
    }

    public ProcessHistoryInfos(JSONArray processInstances) {
        addAll(processInstances);
    }

    void addAll(JSONArray processInstances) {
        if (processInstances != null) {
            for(int i = 0; i < processInstances.length(); i++) {
                try {
                    JSONObject process = processInstances.getJSONObject(i);
                    ProcessHistoryInfo result = new ProcessHistoryInfo(process);
                    processHistories.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
