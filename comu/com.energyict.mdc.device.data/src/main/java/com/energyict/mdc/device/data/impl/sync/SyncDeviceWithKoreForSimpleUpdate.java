/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.data.impl.sync;

import com.elster.jupiter.domain.util.HasNoBlacklistedCharacters;
import com.elster.jupiter.domain.util.HasNotAllowedChars;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Just update the (mdc) device's counterpart (pulse) enddevice
 */
public class SyncDeviceWithKoreForSimpleUpdate extends AbstractSyncDeviceWithKoreMeter {

    public enum Field {
        MANUFACTURER() {
            @Override
            public void invoke(Meter meter, Optional value) {
                value.ifPresent(
                        v -> {
                            if (v instanceof String){
                                meter.setManufacturer((String)v);
                            }
                        }
                );
            }
        },
        MODELNBR() {
            @Override
            public void invoke(Meter meter, Optional value) {
                value.ifPresent(
                        v -> {
                            if (v instanceof String){
                                meter.setModelNumber((String)v);
                            }
                        }
                );
            }
        },
        MODELVERSION() {
            @Override
            public void invoke(Meter meter, Optional value) {
                value.ifPresent(
                        v -> {
                            if (v instanceof String){
                                meter.setModelVersion((String)v);
                            }
                        }
                );
            }
        },
        SERIALNUMBER() {
            @Override
            public void invoke(Meter meter, Optional value) {
                value.ifPresent(
                        v -> {
                            if (v instanceof String){
                                meter.setSerialNumber((String)v);
                            }
                        }
                );
            }
        },
        LOCATION() {
            @Override
            public void invoke(Meter meter, Optional value) {
                Location location = null;
                if (value.isPresent() && (value.get() instanceof Location)){
                    location = (Location)value.get();
                }
                meter.setLocation(location);
            }
        },
        SPATIALCOORDINATES() {
            @Override
            public void invoke(Meter meter, Optional value) {
                SpatialCoordinates spatialCoordinates = null;
                if (value.isPresent() && (value.get() instanceof SpatialCoordinates)){
                    spatialCoordinates = (SpatialCoordinates)value.get();
                }
                meter.setSpatialCoordinates(spatialCoordinates);
            }
        };
        public abstract void invoke(Meter meter, Optional value);
    }

    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String manufacturer;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String modelNbr;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String modelVersion;
    @Size(max = Table.NAME_LENGTH, groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_TOO_LONG + "}")
    @HasNoBlacklistedCharacters(balcklistedCharRegEx = HasNotAllowedChars.Constant.SCRIPT_CHARS)
    private String serialNumber;
    private Optional<Location> location = Optional.empty();
    private Optional<SpatialCoordinates> spatialCoordinates = Optional.empty();
    private Map<Field, Optional> dirtyFields = new HashMap<>();

    public SyncDeviceWithKoreForSimpleUpdate(DeviceImpl device, ServerDeviceService deviceService, MdcReadingTypeUtilService readingTypeUtilService, EventService eventService) {
        super(deviceService, readingTypeUtilService, eventService, null);
    }

    public void setLocation(Location location) {
        this.location = Optional.ofNullable(location);
        dirtyFields.put(Field.LOCATION, this.location);
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
        dirtyFields.put(Field.MANUFACTURER, Optional.ofNullable(manufacturer));
    }

    public String getModelNumber() {
        return modelNbr;
    }

    public void setModelNumber(String modelNumber) {
        this.modelNbr = modelNumber;
        dirtyFields.put(Field.MODELNBR, Optional.ofNullable(modelNumber));
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
        dirtyFields.put(Field.MODELVERSION, Optional.ofNullable(modelVersion));
    }

    public void setSpatialCoordinates(SpatialCoordinates spatialCoordinates) {
        this.spatialCoordinates = Optional.ofNullable(spatialCoordinates);
        dirtyFields.put(Field.SPATIALCOORDINATES, this.spatialCoordinates);
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
        dirtyFields.put(Field.SERIALNUMBER, Optional.ofNullable(serialNumber));
    }

    @Override
    public void syncWithKore(DeviceImpl device) {
        Meter meter = device.getMeterReference().get();
        dirtyFields
                .entrySet()
                .stream()
                .forEach(field -> field.getKey().invoke(meter, field.getValue()));
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
