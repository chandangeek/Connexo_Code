package com.energyict.mdc.device.alarms.rest.response;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AlarmProcessInfos {

    public List<AlarmProcessInfo> processes = new ArrayList<>();

    public AlarmProcessInfos() {
    }

    public AlarmProcessInfos(JSONArray processInstances) {
        addAll(processInstances);
    }

    void addAll(JSONArray processInstances) {
        if (processInstances != null) {
            for(int i = 0; i < processInstances.length(); i++) {
                try {
                    JSONObject process = processInstances.getJSONObject(i);
                    AlarmProcessInfo result = new AlarmProcessInfo(process);
                    processes.add(result);
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

}
