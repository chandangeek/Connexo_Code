/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.system;

public enum ComponentStatus {
    ACTIVE("active"),
    INSTALLED("installed"),
    RESOLVED("resolved"),
    STARTING("starting"),
    STOPPING("stopping"),
    UNINSTALLED("uninstalled");

    private final String id;

    ComponentStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }
}
