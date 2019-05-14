package com.elster.jupiter.bpm.rest.impl;

import java.time.Instant;

import org.json.JSONException;
import org.json.JSONObject;

import com.elster.jupiter.util.json.JsonDeserializeException;

public class ProcessInstance {

    public String realClass;
    public String id;
    public Long processInstanceId;
    public String processId;
    public Instant start;
    public Instant end;
    public int status;
    public Long parentProcessInstanceId;
    public String outcome;
    public Long duration;
    public String identity;
    public String processVersion;
    public String processName;
    public String externalId;
    public String processInstanceDescription;

    public ProcessInstance(JSONObject jsonObject) {
        try {
            this.realClass = jsonObject.getString("realClass");
            this.id = jsonObject.getString("id");
            this.processInstanceId = jsonObject.getLong("processInstanceId");
            this.processId = jsonObject.getString("processId");
            Object startMillis = jsonObject.get("start");
            this.start = (startMillis instanceof Long) ? Instant.ofEpochMilli((Long) startMillis) : null;
            Object endMillis = jsonObject.get("end");
            this.end = (endMillis instanceof Long) ? Instant.ofEpochMilli((Long) endMillis) : null;
            this.status = jsonObject.getInt("status");
            this.parentProcessInstanceId = jsonObject.getLong("parentProcessInstanceId");
            Object durationObject = jsonObject.get("duration");
            this.duration = (durationObject instanceof Long) ? (Long) durationObject : null;
            this.identity = jsonObject.getString("identity");
            this.processVersion = jsonObject.getString("processVersion");
            this.processName = jsonObject.getString("processName");
            this.externalId = jsonObject.getString("externalId");
            this.processInstanceDescription = jsonObject.getString("processInstanceDescription");
        } catch (JSONException e) {
            throw new JsonDeserializeException(e, jsonObject.toString(), ProcessInstance.class);
        }
    }

}
