/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;


public class ServiceCallChildrenInfo {
    public String state;
    public String stateDisplayName;
    public long percentage;
    public long count;

    public ServiceCallChildrenInfo() {
    }

    public ServiceCallChildrenInfo(String state, String stateDisplayName, long percentage, long count) {
        this.state = state;
        this.stateDisplayName = stateDisplayName;
        this.percentage = percentage;
        this.count = count;
    }
}
