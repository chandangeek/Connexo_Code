package com.elster.jupiter.metering.rest;

import com.elster.jupiter.cbo.ElectronicAddress;
import com.elster.jupiter.metering.Meter;

public class MeterInfo {

    public long id;
    public String aliasName;
    public String description;
    public String mRID;
    public String name;
    public String serialNumber;
    public String utcNumber;
    public ElectronicAddress electronicAddress;
    public long version;
    public AmrSystemInfo amrSystem;

    public MeterInfo(Meter meter) {
        this.id = meter.getId();
        this.aliasName = meter.getAliasName();
        this.description = meter.getDescription();
        this.mRID = meter.getMRID();
        this.name = meter.getName();
        this.serialNumber = meter.getSerialNumber();
        this.utcNumber = meter.getUtcNumber();
        this.electronicAddress = meter.getElectronicAddress();
        this.version = meter.getVersion();
    }
}
