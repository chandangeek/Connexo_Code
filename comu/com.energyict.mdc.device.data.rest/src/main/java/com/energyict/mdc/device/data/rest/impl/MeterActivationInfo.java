package com.energyict.mdc.device.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.energyict.mdc.device.data.Device;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.time.Instant;

@XmlRootElement
public class MeterActivationInfo {

    public long id;
    public long version;
    public boolean active;
    public Instant start;
    public Instant end;
    public Long multiplier;
    public IdWithNameInfo usagePoint;
    public IdWithNameInfo deviceConfiguration;

    public MeterActivationInfo() {
    }

    public MeterActivationInfo(MeterActivation meterActivation, Device device) {
        this.id = meterActivation.getId();
        this.version = meterActivation.getVersion();
        this.active = meterActivation.isCurrent();
        this.start = meterActivation.getStart();
        this.end = meterActivation.getEnd();
        this.multiplier = meterActivation.getMultipliers().values().stream().findFirst().map(BigDecimal::longValue).orElse(null);
        this.usagePoint = meterActivation.getUsagePoint().map(up -> new IdWithNameInfo(up.getId(), up.getMRID())).orElse(null);
        this.deviceConfiguration = device.getHistory(Instant.ofEpochMilli(meterActivation.getStart().toEpochMilli() + 1000)).map(d -> new IdWithNameInfo(d.getDeviceConfiguration().getId(), d.getDeviceConfiguration().getName())).orElse(null);
    }
}

