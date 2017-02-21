/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import java.time.Instant;
import java.util.Optional;

/**
 * Just update the (mdc) device's counterpart (pulse) enddevice
 */
public class SyncDeviceWithKoreForSimpleUpdate extends AbstractSyncDeviceWithKoreMeter {

    private Optional<Location> location = Optional.empty();
    private Optional<SpatialCoordinates> spatialCoordinates = Optional.empty();

    public SyncDeviceWithKoreForSimpleUpdate(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, null);
    }

    public void setLocation(Location location) {
        this.location = Optional.ofNullable(location);
    }

    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        this.spatialCoordinates = Optional.ofNullable(spatialCoordinates);
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        if (this.location.isPresent()) {
            device.getMeter().get().setLocation(location.get());
        }
        if (this.spatialCoordinates.isPresent()) {
            device.getMeter().get().setSpatialCoordinates(spatialCoordinates.get());
        }
        device.getMeter().getOptional().get().update();
    }

    @Override
    protected MeterActivation doActivateMeter(Instant generalizedStartDate) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean canUpdateCurrentMeterActivation() {
        return false;
    }

    public Optional<Location> getLocation() {
        return location;
    }

    public Optional<SpatialCoordinates> getSpatialCoordinates() {
        return spatialCoordinates;
    }
}
