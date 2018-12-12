/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.usagepoint.lifecycle.rest;

public class BusinessProcessInfo {

    public long id;
    public String name;
    public String version;

    public BusinessProcessInfo() {
    }

    public BusinessProcessInfo(long id, String name, String version) {
        this.id = id;
        this.name = name;
        this.version = version;
    }
}
