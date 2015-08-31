package com.elster.insight.usagepoint.data.rest.impl;


import java.time.Instant;
import java.util.Optional;

import com.elster.jupiter.metering.LifecycleDates;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.UsagePoint;

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
        
        UsagePoint usagePoint = getUsagePoint(meter);
        if (usagePoint != null) {
     		usagePointName = usagePoint.getName();
     		usagePointMRId = usagePoint.getMRID();
        }
    }
    
    private UsagePoint getUsagePoint(Meter meter) {
    	 Optional<? extends MeterActivation> activations = meter.getCurrentMeterActivation();
         if (activations.isPresent()) {
         	Optional<UsagePoint> ausagePoint = activations.get().getUsagePoint();
         	if (ausagePoint.isPresent()) {
         		return ausagePoint.get();
         	}
         }
         return null;
    }
}
