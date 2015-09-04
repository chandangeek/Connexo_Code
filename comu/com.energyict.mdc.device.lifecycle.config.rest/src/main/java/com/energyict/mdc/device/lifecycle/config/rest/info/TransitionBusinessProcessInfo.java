package com.energyict.mdc.device.lifecycle.config.rest.info;

public class TransitionBusinessProcessInfo {

    public long id;
    public String name;
    public String deploymentId;
    public String processId;

    public TransitionBusinessProcessInfo() {
    }

    public TransitionBusinessProcessInfo(long id, String name, String deploymentId, String processId) {
        this.id = id;
        this.name = name;
        this.deploymentId = deploymentId;
        this.processId = processId;
    }
}
