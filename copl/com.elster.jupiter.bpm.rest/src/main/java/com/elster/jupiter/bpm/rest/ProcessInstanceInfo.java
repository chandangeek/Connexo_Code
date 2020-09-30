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
            this.id = instance.getString("id");
            this.name = instance.getString("processName");
            this.initiator = instance.getString("identity");
            this.version = instance.getString("processVersion");
            this.state = Integer.parseInt(instance.getString("status"));
            this.startDate = DateConvertor.convertTimeStamps(instance.getString("start"), false);
            this.endDate = DateConvertor.convertTimeStamps(instance.getString("end"), false);
            this.deploymentId = instance.getString("externalId");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
