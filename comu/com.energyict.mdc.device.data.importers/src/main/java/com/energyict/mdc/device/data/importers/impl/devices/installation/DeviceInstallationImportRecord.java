package com.energyict.mdc.device.data.importers.impl.devices.installation;

import com.energyict.mdc.device.data.importers.impl.devices.DeviceTransitionRecord;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class DeviceInstallationImportRecord extends DeviceTransitionRecord {

    private String usagePointIdentifier;
    private String serviceCategory;
    private boolean installInactive;
    private ZonedDateTime startValidationDate;
    private BigDecimal multiplier;

    public String getUsagePointIdentifier() {
        return usagePointIdentifier;
    }

    public void setUsagePointIdentifier(String usagePointIdentifier) {
        this.usagePointIdentifier = usagePointIdentifier;
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

    public BigDecimal getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(BigDecimal multiplier) {
        this.multiplier = multiplier;
    }
}
