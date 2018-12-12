/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.kpi.rest.impl;

import com.energyict.mdc.device.topology.kpi.RegisteredDevicesKpiScore;

import java.time.Instant;

public class RegisteredDevicesKpiGraphInfo {
    public Instant timestamp;
    public long target;
    public long total;
    public long registered;

    public RegisteredDevicesKpiGraphInfo(RegisteredDevicesKpiScore score, long target) {
        this.timestamp = score.getTimestamp();
        this.target = score.getTarget(target);
        this.total = score.getTotal().longValue();
        this.registered = score.getRegistered().longValue();
    }

    public RegisteredDevicesKpiGraphInfo(RegisteredDevicesKpiScore score) {
        this.timestamp = score.getTimestamp();
        this.registered = score.getRegistered().longValue();
    }
}
