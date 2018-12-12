/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.importers.impl.devices;

import com.energyict.mdc.device.data.importers.impl.FileImportRecord;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Optional;

public class DeviceTransitionRecord extends FileImportRecord {

    private ZonedDateTime transitionDate;
    private String masterDeviceIdentifier;

    public Optional<Instant> getTransitionDate() {
        return Optional.ofNullable(this.transitionDate != null ? this.transitionDate.toInstant() : null);
    }

    public void setTransitionDate(ZonedDateTime transitionDate) {
        this.transitionDate = transitionDate;
    }

    public String getMasterDeviceIdentifier() {
        return masterDeviceIdentifier;
    }

    public void setMasterDeviceIdentifier(String masterDeviceIdentifier) {
        this.masterDeviceIdentifier = masterDeviceIdentifier;
    }

    public Optional<Instant> getTransitionActionDate() {
        return getTransitionDate();
    }
}
