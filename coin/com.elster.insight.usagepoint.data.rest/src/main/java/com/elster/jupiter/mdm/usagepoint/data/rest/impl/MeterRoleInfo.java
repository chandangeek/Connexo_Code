/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.MeterRole;

import java.time.Instant;

public class MeterRoleInfo {
    public String id;
    public String name;
    public boolean required;
    public String meter;
    public String url;
    public Instant activationTime;

    public MeterRoleInfo() {
    }

    public MeterRoleInfo(MeterRole meterRole) {
        this.id = meterRole.getKey();
        this.name = meterRole.getDisplayName();
    }
}
