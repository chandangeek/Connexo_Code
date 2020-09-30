/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class DeploymentInfo {

    public String identifier;

    public DeploymentInfo() {
    }

    public DeploymentInfo(JSONObject deployment) {
        try {
            this.identifier = deployment.getString("identifier");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
