package com.elster.jupiter.bpm;


import java.util.Map;

public class BpmProcess{

    private String deploymentId;
    private long id;
    private String name;
    private String id2;
    private Map<String, Object> parameters;

    @SuppressWarnings("unused")
    private BpmProcess(){

    }

    public BpmProcess(String deploymentId, String id2, Map<String, Object> parameters){
        this.deploymentId = deploymentId;
        this.id2 = id2;
        this.parameters = parameters;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public String getId() {
        return this.id2;
    }

    public Map<String, Object> getParameters() {
        return this.parameters;
    }
}
