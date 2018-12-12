/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm.rest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.List;

@XmlRootElement
public class DeploymentInfos {
    public int total;

    public List<DeploymentInfo> deployments = new ArrayList<>();

    public DeploymentInfos() {
    }

    public DeploymentInfos(JSONArray deployments) {
        addAll(deployments);
    }

    void addAll(JSONArray deploymentList) {
        if (deploymentList != null) {
            for(int i = 0; i < deploymentList.length(); i++) {
                try {
                    JSONObject deployment = deploymentList.getJSONObject(i);
                    DeploymentInfo result = new DeploymentInfo(deployment);
                    deployments.add(result);
                    total++;
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public List<DeploymentInfo> getDeployments() {
        return deployments;
    }
}
