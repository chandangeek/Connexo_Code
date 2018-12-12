/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.systemadmin.rest.imp.response;

public enum LicenseProperties {
    USERS("licensed.users"),
    DEVICE("licensed.device"),
    PROTOCOLS("licensed.protocols");

    private String name;

    LicenseProperties(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
