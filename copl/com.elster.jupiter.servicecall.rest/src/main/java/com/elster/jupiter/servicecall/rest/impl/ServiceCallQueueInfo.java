/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.servicecall.rest.impl;


public class ServiceCallQueueInfo {
    public String name;
    public boolean isDefault;

    public ServiceCallQueueInfo() {
    }

    public ServiceCallQueueInfo(String name, boolean isDefault) {
        this.name = name;
        this.isDefault = isDefault;
    }
}
