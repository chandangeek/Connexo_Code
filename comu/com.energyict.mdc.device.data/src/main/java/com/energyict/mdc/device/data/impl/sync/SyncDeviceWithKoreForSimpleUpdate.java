/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.Meter;
import com.elster.jupiter.metering.MeterActivation;
import com.elster.jupiter.orm.Table;
import com.elster.jupiter.util.geo.SpatialCoordinates;
import com.energyict.mdc.device.data.impl.DeviceImpl;
import com.energyict.mdc.device.data.impl.MessageSeeds;
import com.energyict.mdc.device.data.impl.ServerDeviceService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;

import javax.validation.constraints.Size;
import java.time.Instant;
import java.util.Optional;

/**
 * Just update the (mdc) device's counterpart (pulse) enddevice
 */
public class SyncDeviceWithKoreForSimpleUpdate extends AbstractSyncDeviceWithKoreMeter {

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String manufacturer;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String modelNbr;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String modelVersion;
    private Optional<Location> location = Optional.empty();
    private Optional<SpatialCoordinates> spatialCoordinates = Optional.empty();

    public SyncDeviceWithKoreForSimpleUpdate(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, null);
    }

    public void setLocation(Location location) {
        this.location = Optional.ofNullable(location);
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModelNumber() {
        return modelNbr;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNbr = modelNumber;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        this.spatialCoordinates = Optional.ofNullable(spatialCoordinates);
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        Meter meter = device.getMeter(). get();
        meter.setManufacturer(manufacturer);
        meter.setModelNumber(modelNbr);
        meter.setModelVersion(modelVersion);
        if (this.location.isPresent()) {
            device.getMeter().get().setLocation(location.get());
        }
        if (this.spatialCoordinates.isPresent()) {
            device.getMeter().get().setSpatialCoordinates(spatialCoordinates.get());
        }
        meter.update();
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
