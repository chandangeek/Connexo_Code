package com.energyict.mdc.device.data.importers.impl.devices;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class DeviceTransitionRecord extends FileImportRecord {

    private ZonedDateTime transitionDate;
    private String masterDeviceMrid;

    public Optional<Instant> getTransitionDate() {
        return Optional.ofNullable(this.transitionDate != null ? this.transitionDate.toInstant() : null);
    }

    public void setTransitionDate(ZonedDateTime transitionDate) {
        this.transitionDate = transitionDate;
    }

    public String getMasterDeviceMrid() {
        return masterDeviceMrid;
    }

    public void setMasterDeviceMrid(String masterDeviceMrid) {
        this.masterDeviceMrid = masterDeviceMrid;
    }

    public Optional<Instant> getTransitionActionDate() {
        return getTransitionDate();
    }
}
