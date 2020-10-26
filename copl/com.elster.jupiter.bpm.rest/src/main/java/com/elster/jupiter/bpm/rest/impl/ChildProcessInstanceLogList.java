package com.elster.jupiter.bpm.rest.impl;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.elster.jupiter.util.json.JsonDeserializeException;

public class ChildProcessInstanceLogList {

    public List<ChildProcessInstanceLog> childProcessInstanceLogList = new ArrayList<>();

    public ChildProcessInstanceLogList() {
    }

    public ChildProcessInstanceLogList(JSONObject jsonObject) {
        JSONArray jsonArray = null;
        try {
            jsonArray = jsonObject.getJSONArray("processInstanceLogs");
            if (jsonArray != null && jsonArray.length() > 0) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.getJSONObject(i);
                    ChildProcessInstanceLog childProcessInstanceLog = new ChildProcessInstanceLog(object);
                    this.childProcessInstanceLogList.add(childProcessInstanceLog);
                }
            }
        } catch (JSONException e) {
            throw new JsonDeserializeException(e, jsonObject.toString(), ChildProcessInstanceLogList.class);
        }
    }
}
