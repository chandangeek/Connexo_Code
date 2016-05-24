package com.elster.jupiter.mdm.usagepoint.data.rest.impl;


import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MeterInfo {
	
    public long id;
    public String aliasName;
    public String description;
    public String mRID;
    public String name;
    public String serialNumber;
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
    public List meterActivations;
    
    public MeterInfo() {
    }
    
    public MeterInfo(Meter meter) {
        this.id = meter.getId();
        this.aliasName = meter.getAliasName();
        this.description = meter.getDescription();
        this.mRID = meter.getMRID();
        this.name = meter.getName();
        this.serialNumber = meter.getSerialNumber();
        this.utcNumber = meter.getUtcNumber();
        this.meterActivations = meter.getMeterActivations()
                .stream()
                .map(ma -> new MeterActivationInfo(ma, false))
                .collect(Collectors.toList());

        if (meter.getElectronicAddress() != null) {
        	this.eMail1 = meter.getElectronicAddress().getEmail1();
        	this.eMail2 = meter.getElectronicAddress().getEmail2();
        }
        this.amrSystemName = meter.getAmrSystem().getName();
        this.version = meter.getVersion();
        LifecycleDates lcd = meter.getLifecycleDates();
        if (lcd != null) {
        	Optional<Instant> mInstalledDate =lcd.getInstalledDate();
        	Optional<Instant> mRemovedDate =lcd.getRemovedDate();
        	Optional<Instant> mRetiredDate =lcd.getRetiredDate();
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
        
        Optional<UsagePoint> usagePoint = meter.getCurrentMeterActivation().flatMap(MeterActivation::getUsagePoint);
        if (usagePoint.isPresent()) {
     		usagePointName = usagePoint.get().getName();
     		usagePointMRId = usagePoint.get().getMRID();
        }
    }
    
}
