package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.MeterActivation;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class MeterActivationInfo {

    public long id;
    public Long start;
    public Long end;
    public long version;
    public MeterInfo meter;

    public MeterActivationInfo() {
    }

    public MeterActivationInfo(MeterActivation meterActivation) {
        this.id = meterActivation.getId();
        this.start = meterActivation.getStart() == null ? null : meterActivation.getStart().toEpochMilli();
        this.end = meterActivation.getEnd() == null ? null : meterActivation.getEnd().toEpochMilli();
        this.version = meterActivation.getVersion();
        this.meter = meterActivation.getMeter().isPresent() ? new MeterInfo(meterActivation.getMeter().get()) : null;
    }
}