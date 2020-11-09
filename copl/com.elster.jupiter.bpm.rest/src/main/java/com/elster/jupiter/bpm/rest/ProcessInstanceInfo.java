/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import com.elster.jupiter.bpm.rest.impl.DateConvertor;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ProcessInstanceInfo {

    public String id;
    public String name;
    public String initiator;
    public String version;
    public int state;
    public String startDate;
    public String endDate;
    public String deploymentId;

    public ProcessInstanceInfo() {
    }

    public ProcessInstanceInfo(JSONObject instance) {
        try {
            this.id = instance.getString("process-instance-id");
            this.name = instance.getString("process-name");
            this.initiator = instance.getString("initiator");
            this.version = instance.getString("process-version");
            this.state = Integer.parseInt(instance.getString("process-instance-state"));
            this.startDate = DateConvertor.convertTimeStamps(instance.getJSONObject("start-date").getString("java.util.Date"), false);
            this.endDate = DateConvertor.convertTimeStamps(instance.getString("sla-due-date"), false);
            this.deploymentId = instance.getString("container-id");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
