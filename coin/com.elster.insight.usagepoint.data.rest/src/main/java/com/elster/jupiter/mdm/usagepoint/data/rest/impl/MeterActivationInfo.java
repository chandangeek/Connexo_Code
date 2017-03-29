/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Instant;

@XmlRootElement
public class MeterActivationInfo {

    public long id;
    public Instant start;
    public Instant end;
    public long version;
    public MeterInfo meter;
    public MeterRoleInfo meterRole;
    public UsagePointInfo usagePoint;

    public MeterActivationInfo() {
    }

    public MeterActivationInfo(MeterActivation meterActivation, boolean includeMeterInfo) {
        this.id = meterActivation.getId();
        this.start = meterActivation.getStart() == null ? null : meterActivation.getStart();
        this.end = meterActivation.getEnd() == null ? null : meterActivation.getEnd();
        this.version = meterActivation.getVersion();
        meterActivation.getMeter().ifPresent(m -> {
            this.meter = includeMeterInfo ? new MeterInfo(m) : new MeterInfo();
            this.meter.name = m.getName();
        });
        meterActivation.getMeterRole().ifPresent(mr -> this.meterRole = new MeterRoleInfo(mr));
        meterActivation.getUsagePoint().ifPresent(up -> {
            this.usagePoint = new UsagePointInfo();
            this.usagePoint.name = up.getName();
        });
    }
}
