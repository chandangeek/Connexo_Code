package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class DeviceInstallationImportRecord extends FileImportRecord {

    private ZonedDateTime installationDate;
    private String masterDeviceMrid;
    private String usagePointMrid;
    private String serviceCategory;
    private boolean installInactive;
    private ZonedDateTime startValidationDate;

    public ZonedDateTime getInstallationDate() {
        return installationDate;
    }

    public void setInstallationDate(ZonedDateTime installationDate) {
        this.installationDate = installationDate;
    }

    public String getMasterDeviceMrid() {
        return masterDeviceMrid;
    }

    public void setMasterDeviceMrid(String masterDeviceMrid) {
        this.masterDeviceMrid = masterDeviceMrid;
    }

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

    public Instant getStartValidationDate() {
        return Optional.ofNullable(startValidationDate).map(ZonedDateTime::toInstant).orElseGet(getInstallationDate()::toInstant);
    }

    public void setStartValidationDate(ZonedDateTime startValidationDate) {
        this.startValidationDate = startValidationDate;
    }
}
