/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;


import java.util.Map;

public class BpmProcess{

    private String deploymentId;
    private String id;
    private String auth;
    private Map<String, Object> parameters;

    @SuppressWarnings("unused")
    private BpmProcess(){

    }

    public BpmProcess(String deploymentId, String id, Map<String, Object> parameters){
        this.deploymentId = deploymentId;
        this.id = id;
        this.parameters = parameters;
    }

    public BpmProcess(String deploymentId, String id, Map<String, Object> parameters, String auth){
        this.deploymentId = deploymentId;
        this.id = id;
        this.parameters = parameters;
        this.auth = auth;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getId() {
        return this.id;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }

    public String getAuth() {
        return auth;
    }
}
