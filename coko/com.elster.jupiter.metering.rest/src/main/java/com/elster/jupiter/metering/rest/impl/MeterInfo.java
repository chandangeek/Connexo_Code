package com.elster.jupiter.metering.rest.impl;


import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;

import java.time.Instant;
import java.util.Optional;

public class MeterInfo {

    public long id;
    public String aliasName;
    public String description;
    public String mRID;
    public String name;
    public String serialNumber;
    public String manufacturer;
    public String modelNbr;
    public String modelVersion;
    public String utcNumber;
    public String eMail1;
    public String eMail2;
    public long version;
    public String amrSystemName;
    public String usagePointMRId;
    public String usagePointName;
    public long installedDate;
    public long removedDate;
    public long retiredDate;

    public MeterInfo() {
    }

    public MeterInfo(Meter meter) {
        this.id = meter.getId();
        this.aliasName = meter.getAliasName();
        this.description = meter.getDescription();
        this.mRID = meter.getMRID();
        this.name = meter.getName();
        this.serialNumber = meter.getSerialNumber();
        this.manufacturer = meter.getManufacturer();
        this.modelNbr = meter.getModelNumber();
        this.modelVersion = meter.getModelVersion();
        this.utcNumber = meter.getUtcNumber();

        if (meter.getElectronicAddress() != null) {
            this.eMail1 = meter.getElectronicAddress().getEmail1();
            this.eMail2 = meter.getElectronicAddress().getEmail2();
        }
        this.amrSystemName = meter.getAmrSystem().getName();
        this.version = meter.getVersion();
        LifecycleDates lcd = meter.getLifecycleDates();
        if (lcd != null) {
            Optional<Instant> mInstalledDate = lcd.getInstalledDate();
            Optional<Instant> mRemovedDate = lcd.getRemovedDate();
            Optional<Instant> mRetiredDate = lcd.getRetiredDate();
            if (mInstalledDate.isPresent()) {
                this.installedDate = mInstalledDate.get().getEpochSecond();
            }
            if (mRemovedDate.isPresent()) {
                this.removedDate = mRemovedDate.get().getEpochSecond();
            }
            if (mRetiredDate.isPresent()) {
                this.retiredDate = mRetiredDate.get().getEpochSecond();
            }
        }

        meter.getCurrentMeterActivation()
                .flatMap(MeterActivation::getUsagePoint)
                .ifPresent(usagePoint -> {
                    usagePointName = usagePoint.getName();
                    usagePointMRId = usagePoint.getMRID();
                });
    }
}
