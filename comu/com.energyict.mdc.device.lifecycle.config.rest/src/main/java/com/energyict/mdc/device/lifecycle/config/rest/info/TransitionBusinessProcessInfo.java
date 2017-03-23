/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.lifecycle.config.rest.info;

public class TransitionBusinessProcessInfo {

    public long id;
    public String name;
    public String version;

    public TransitionBusinessProcessInfo() {
    }

    public TransitionBusinessProcessInfo(long id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }
}
