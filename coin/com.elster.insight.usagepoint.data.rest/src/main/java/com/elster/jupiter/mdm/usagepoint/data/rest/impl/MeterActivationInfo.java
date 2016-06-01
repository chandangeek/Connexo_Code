package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MeterActivationInfo {

    public long id;
    public Long start;
    public Long end;
    public long version;
    public MeterInfo meter;
    public MeterRoleInfo meterRole;
    public UsagePointInfo usagePoint;

    public MeterActivationInfo() {
    }

    public MeterActivationInfo(MeterActivation meterActivation, boolean includeMeterInfo) {
        this.id = meterActivation.getId();
        this.start = meterActivation.getStart() == null ? null : meterActivation.getStart().toEpochMilli();
        this.end = meterActivation.getEnd() == null ? null : meterActivation.getEnd().toEpochMilli();
        this.version = meterActivation.getVersion();
        meterActivation.getMeter().ifPresent(meter -> {
            this.meter = includeMeterInfo ? new MeterInfo(meterActivation.getMeter().get()) : new MeterInfo();
            this.meter.mRID = meterActivation.getMeter().get().getMRID();
        });
        meterActivation.getMeterRole().ifPresent(mr -> this.meterRole = new MeterRoleInfo(mr));
        meterActivation.getUsagePoint().ifPresent(up -> {
            this.usagePoint = new UsagePointInfo();
            this.usagePoint.mRID = meterActivation.getUsagePoint().get().getMRID();
        });
    }
}