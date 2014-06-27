package com.elster.jupiter.bpm.rest;

import org.codehaus.jackson.annotate.JsonProperty;
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
