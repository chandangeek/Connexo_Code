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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Just update the (mdc) device's counterpart (pulse) enddevice
 */
public class SyncDeviceWithKoreForSimpleUpdate extends AbstractSyncDeviceWithKoreMeter {

    public enum Fields {
        MANUFACTURER {
            @Override
            public void invoke(Meter meter) {
                meter.setManufacturer(value);
            }
        },
        MODELNBR {
            @Override
            public void invoke(Meter meter) {
                meter.setModelNumber(value);
            }
        },
        MODELVERSION {
            @Override
            public void invoke(Meter meter) {
                meter.setModelVersion(value);
            }
        },
        SERIALNUMBER {
            @Override
            public void invoke(Meter meter) {
                meter.setSerialNumber(value);
            }
        },
        LOCATION {
            @Override
            public void invoke(Meter meter) {
                meter.setLocation(this.location.isPresent() ? location.get() : null);
            }
        },
        SPATIALCOORDINATES {
            @Override
            public void invoke(Meter meter) {
                meter.setSpatialCoordinates(this.spatialCoordinates.isPresent() ? spatialCoordinates.get() : null);
            }
        };

        protected String value;
        protected Optional<Location> location = Optional.empty();
        protected Optional<SpatialCoordinates> spatialCoordinates = Optional.empty();

        public abstract void invoke(Meter meter);

        public Fields setValue(String value) {
            this.value = value;
            return this;
        }

        public Fields setLocation(Location location) {
            this.location = Optional.ofNullable(location);
            return this;
        }

        public Fields setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
            this.spatialCoordinates = Optional.ofNullable(spatialCoordinates);
            return this;
        }
    }

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String manufacturer;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String modelNbr;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String modelVersion;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    private String serialNumber;
    private Optional<Location> location = Optional.empty();
    private Optional<SpatialCoordinates> spatialCoordinates = Optional.empty();
    private List<Fields> dirtyFields = new ArrayList();

    public SyncDeviceWithKoreForSimpleUpdate(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, null);
    }

    public void setLocation(Location location) {
        this.location = Optional.ofNullable(location);
        dirtyFields.add(Fields.LOCATION.setLocation(location));
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        dirtyFields.add(Fields.MANUFACTURER.setValue(manufacturer));
    }

    public String getModelNumber() {
        return modelNbr;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNbr = modelNumber;
        dirtyFields.add(Fields.MODELNBR.setValue(modelNumber));
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        dirtyFields.add(Fields.MODELVERSION.setValue(modelVersion));
    }

    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        this.spatialCoordinates = Optional.ofNullable(spatialCoordinates);
        dirtyFields.add(Fields.SPATIALCOORDINATES.setSpatialCoordinates(spatialCoordinates));
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        dirtyFields.add(Fields.SERIALNUMBER.setValue(serialNumber));
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        Meter meter = device.getMeterReference().get();
        dirtyFields.stream().forEach(field -> field.invoke(meter));
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
