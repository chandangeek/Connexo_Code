package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class DeviceInstallationImportRecord extends DeviceTransitionRecord {

    private String usagePointMrid;
    private String serviceCategory;
    private boolean installInactive;
    private ZonedDateTime startValidationDate;

    public String getUsagePointMrid() {
        return usagePointMrid;
    }

    public void setUsagePointMrid(String usagePointMrid) {
        this.usagePointMrid = usagePointMrid;
    }

    public String getServiceCategory() {
        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {
        this.serviceCategory = serviceCategory;
    }

    public boolean isInstallInactive() {
        return installInactive;
    }

    public void setInstallInactive(boolean installInactive) {
        this.installInactive = installInactive;
    }

    public Optional<Instant> getTransitionActionDate() {
        return this.startValidationDate != null ? Optional.of(this.startValidationDate.toInstant()) : getTransitionDate();
    }

    public void setStartValidationDate(ZonedDateTime startValidationDate) {
        this.startValidationDate = startValidationDate;
    }
}
