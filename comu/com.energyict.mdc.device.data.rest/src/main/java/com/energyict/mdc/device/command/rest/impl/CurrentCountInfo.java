/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.command.rest.impl;

import java.time.Instant;

public class CurrentCountInfo {
    public String type;
    public long currentCount;
    public Instant from;
    public Instant to;

    public CurrentCountInfo() {

    }
}
