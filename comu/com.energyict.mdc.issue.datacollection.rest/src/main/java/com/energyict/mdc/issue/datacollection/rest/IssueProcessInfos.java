/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.issue.datacollection.rest;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IssueProcessInfos {

    public List<IssueProcessInfo> processes = new ArrayList<>();

    public IssueProcessInfos() {
    }

    public IssueProcessInfos(JSONArray processInstances) {
        addAll(processInstances);
    }

    void addAll(JSONArray processInstances) {
        if (processInstances != null) {
            for(int i = 0; i < processInstances.length(); i++) {
                try {
                    JSONObject process = processInstances.getJSONObject(i);
                    IssueProcessInfo result = new IssueProcessInfo(process);
                    processes.add(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
