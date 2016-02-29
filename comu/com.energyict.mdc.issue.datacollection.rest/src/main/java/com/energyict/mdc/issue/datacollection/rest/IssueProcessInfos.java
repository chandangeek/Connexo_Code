package com.energyict.mdc.issue.datacollection.rest;


import com.elster.jupiter.issue.rest.response.IssueCommentInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class IssueProcessInfos {
    public int total;

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
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
