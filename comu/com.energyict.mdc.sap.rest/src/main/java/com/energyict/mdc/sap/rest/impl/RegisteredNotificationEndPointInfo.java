/*
 * Copyright (c) 2020 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.sap.rest.impl;

public class RegisteredNotificationEndPointInfo {
    public long id;
    public String name;
    public long version;

    public RegisteredNotificationEndPointInfo() {
    }

    public RegisteredNotificationEndPointInfo(long id, String name, long version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }
}
