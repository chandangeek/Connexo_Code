package com.elster.jupiter.servicecall.rest.impl;


public class ServiceCallChildrenInfo {
    private String state;
    private String stateDisplayName;
    private Object percentage;

    public ServiceCallChildrenInfo() {
    }

    public ServiceCallChildrenInfo(String state, String stateDisplayName, Object percentage) {
        this.state = state;
        this.stateDisplayName = stateDisplayName;
        this.percentage = percentage;
    }
}
