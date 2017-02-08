/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.EndDevice;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.metering.ServiceCategory;
import com.elster.jupiter.metering.ServiceLocation;
import com.elster.jupiter.metering.UsagePoint;

import java.util.Optional;

public class DeviceInfo {
    private long id;
    private String serialNumber;
    private String name;
    private UsagePointInfo usagePoint;
    private ServiceLocationInfo serviceLocation;
    private ServiceCategoryInfo serviceCategory;
    private long version;

    public DeviceInfo(EndDevice endDevice){
        if (endDevice != null) {
            this.id = endDevice.getId();
            this.name = endDevice.getName();
            this.serialNumber = endDevice.getSerialNumber();
            this.version = endDevice.getVersion();
            fetchDetails(endDevice);
        }
    }

    protected void fetchDetails(EndDevice endDevice){
        if (endDevice != null && Meter.class.isInstance(endDevice)) {
            Meter meter = Meter.class.cast(endDevice);
            Optional<? extends MeterActivation> meterActivation = meter.getCurrentMeterActivation();
            if (meterActivation.isPresent()) {
                setUsagePoint(meterActivation.get().getUsagePoint().orElse(null));
            }
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public ServiceCategoryInfo getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory != null ? new ServiceCategoryInfo(serviceCategory) : null;
    }

    public ServiceLocationInfo getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(ServiceLocation serviceLocation) {
        this.serviceLocation = serviceLocation != null ? new ServiceLocationInfo(serviceLocation) : null;
    }

    public UsagePointInfo getUsagePoint() {
        return usagePoint;
    }

    public void setUsagePoint(UsagePoint usagePoint) {
        if (usagePoint != null) {
            this.usagePoint = new UsagePointInfo(usagePoint);
            this.setServiceLocation(usagePoint.getServiceLocation().orElse(null));
            this.setServiceCategory(usagePoint.getServiceCategory());
        }
    }
}
