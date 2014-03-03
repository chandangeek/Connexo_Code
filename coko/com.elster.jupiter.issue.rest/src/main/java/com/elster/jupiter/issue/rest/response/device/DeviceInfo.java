package com.elster.jupiter.issue.rest.response.device;

import com.elster.jupiter.metering.*;
import com.google.common.base.Optional;

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
            this.setId(endDevice.getId());
            this.setName(endDevice.getName());
            this.setSerialNumber(endDevice.getSerialNumber());
            this.setVersion(endDevice.getVersion());
            fetchDetails(endDevice);
        }
    }

    protected void fetchDetails(EndDevice endDevice){
        if (endDevice != null && Meter.class.isInstance(endDevice)) {
            Meter meter = Meter.class.cast(endDevice);
            Optional<MeterActivation> meterActivation = meter.getCurrentMeterActivation();
            if (meterActivation.isPresent()) {
                Optional<UsagePoint> usagePointRef = meterActivation.get().getUsagePoint();
                if (usagePointRef.isPresent()) {
                    UsagePoint usagePoint = usagePointRef.get();
                    ServiceLocation location = usagePoint.getServiceLocation();
                    ServiceCategory category = usagePoint.getServiceCategory();
                    this.setUsagePoint(usagePoint);
                    this.setServiceLocation(location);
                    this.setServiceCategory(category);
                }
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
        this.usagePoint = new UsagePointInfo(usagePoint);
    }
}
