package com.elster.jupiter.issue.rest.response;

import com.elster.jupiter.metering.*;
import com.google.common.base.Optional;

public class IssueDevice {
    private long id;
    private String sNumber;
    private String name;
    private UsagePointPreviewInfo usagePoint;
    private ServiceLocationPreviewInfo serviceLocation;
    private ServiceCategoryPreviewInfo serviceCategory;
    private long version;

    public IssueDevice(EndDevice endDevice){
        if (endDevice != null) {
            this.setId(endDevice.getId());
            this.setName(endDevice.getName());
            this.setsNumber(endDevice.getSerialNumber());
            this.setVersion(endDevice.getVersion());
            if (Meter.class.isInstance(endDevice)) {
                Meter meter = Meter.class.cast(endDevice);
                Optional<MeterActivation> meterActivation = meter.getCurrentMeterActivation();
                if (meterActivation.isPresent()) {
                    Optional<UsagePoint> upRef = meterActivation.get().getUsagePoint();
                    if (upRef.isPresent()) {
                        UsagePoint up = upRef.get();
                        this.setUsagePoint(up);
                        ServiceLocation location = up.getServiceLocation();
                        this.setServiceLocation(location);
                        ServiceCategory category = up.getServiceCategory();
                        this.setServiceCategory(category);
                    }
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

    public String getsNumber() {
        return sNumber;
    }

    public void setsNumber(String sNumber) {
        this.sNumber = sNumber;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public ServiceCategoryPreviewInfo getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(ServiceCategory serviceCategory) {
        this.serviceCategory = serviceCategory != null ? new ServiceCategoryPreviewInfo(serviceCategory) : null;
    }

    public ServiceLocationPreviewInfo getServiceLocation() {
        return serviceLocation;
    }

    public void setServiceLocation(ServiceLocation serviceLocation) {
        this.serviceLocation = serviceLocation != null ? new ServiceLocationPreviewInfo(serviceLocation) : null;
    }

    public UsagePointPreviewInfo getUsagePoint() {
        return usagePoint;
    }

    public void setUsagePoint(UsagePoint usagePoint) {
        this.usagePoint = new UsagePointPreviewInfo(usagePoint);
    }
}
