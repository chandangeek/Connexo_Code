package com.elster.jupiter.bpm.rest;

import org.json.JSONException;
import org.json.JSONObject;

public class ProcessDefinitionInfo {
    public String name;
    public String id;

    public ProcessDefinitionInfo(){

    }

    public ProcessDefinitionInfo(JSONObject jsonObject) {
        try {
            this.name = jsonObject.getString("name");
            this.id = jsonObject.getString("id");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }
}
