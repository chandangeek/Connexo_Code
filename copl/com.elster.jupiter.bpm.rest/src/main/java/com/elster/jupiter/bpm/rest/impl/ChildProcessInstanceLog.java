package com.elster.jupiter.bpm.rest.impl;

import org.json.JSONException;
import org.json.JSONObject;

import com.elster.jupiter.util.json.JsonDeserializeException;

public class ChildProcessInstanceLog {

    public String realClass;
    public String id;
    public Long childProcessInstanceId;
    public String childProcessId;
    public String startDate;
    public String endDate;
    public String status;
    public String parentProcessInstanceId;
    public String outcome;
    public String duration;
    public String identity;
    public String processVersion;
    public String processName;
    public String externalId;
    public String processInstanceDescription;
    public String index;
    public String commandName;

    public ChildProcessInstanceLog(JSONObject jsonObject) {
        final String elementName = "org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog";// JaxbProcessInstanceLog.class.getName();
        try {
            final JSONObject node = jsonObject.getJSONObject(elementName);
            this.realClass = node.getString("realClass");
            this.id = node.getString("id");
            this.childProcessInstanceId = node.getLong("processInstanceId");
            this.childProcessId = node.getString("processId");
            this.startDate = node.getString("start");
            this.endDate = node.getString("end");
            this.status = node.getString("status");
            this.parentProcessInstanceId = node.getString("parentProcessInstanceId");
            this.outcome = node.getString("outcome");
            this.duration = node.getString("duration");
            this.identity = node.getString("identity");
            this.processVersion = node.getString("processVersion");
            this.processName = node.getString("processName");
            this.externalId = node.getString("externalId");
            this.processInstanceDescription = node.getString("processInstanceDescription");
            this.index = node.getString("index");
            this.commandName = node.getString("commandName");
        } catch (JSONException e) {
            throw new JsonDeserializeException(e, jsonObject.toString(), ChildProcessInstanceLog.class);
        }
    }

}
