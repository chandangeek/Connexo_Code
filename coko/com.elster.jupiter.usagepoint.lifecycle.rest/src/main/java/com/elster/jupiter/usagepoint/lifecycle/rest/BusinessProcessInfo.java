/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

public class BusinessProcessInfo {

    public long id;
    public String name;
    public String deploymentId;
    public String processId;

    public BusinessProcessInfo() {
    }

    public BusinessProcessInfo(long id, String name, String deploymentId, String processId) {
        this.id = id;
        this.name = name;
        this.deploymentId = deploymentId;
        this.processId = processId;
    }
}
