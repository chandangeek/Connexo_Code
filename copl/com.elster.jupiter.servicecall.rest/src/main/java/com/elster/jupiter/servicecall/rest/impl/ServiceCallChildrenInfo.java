package com.elster.jupiter.servicecall.rest.impl;


public class ServiceCallChildrenInfo {
    public String state;
    public String stateDisplayName;
    public Object percentage;

    public ServiceCallChildrenInfo() {
    }

    public ServiceCallChildrenInfo(String state, String stateDisplayName, Object percentage) {
        this.state = state;
        this.stateDisplayName = stateDisplayName;
        this.percentage = percentage;
    }
}
