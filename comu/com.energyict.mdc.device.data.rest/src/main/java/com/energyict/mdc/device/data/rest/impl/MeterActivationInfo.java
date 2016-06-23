package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MeterActivationInfo {

    public long id;
    public Long start;
    public Long end;
    public long version;
    public boolean active;
    public IdWithNameInfo usagePoint;
    public Long multiplier;

    public MeterActivationInfo() {
    }

    public MeterActivationInfo(MeterActivation meterActivation, Device device) {
        this.id = meterActivation.getId();
        if (meterActivation.getStart() != null) {
            this.start = meterActivation.getStart().toEpochMilli();
            this.multiplier = device.getMultiplierAt(meterActivation.getStart()).isPresent() ? device.getMultiplierAt(meterActivation.getStart()).get().longValue() : null;
        }
        this.end = meterActivation.getEnd() == null ? null : meterActivation.getEnd().toEpochMilli();
        this.version = meterActivation.getVersion();
        this.active = meterActivation.isCurrent();
        this.usagePoint = meterActivation.getUsagePoint().isPresent() ?
                new IdWithNameInfo(meterActivation.getUsagePoint().get().getId(), meterActivation.getUsagePoint().get().getName()) : null;
    }
}

