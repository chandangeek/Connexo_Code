/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareType;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;
import java.util.Optional;

public class ActivatedFirmwareVersionImpl implements ActivatedFirmwareVersion {

    public enum Fields {
        FIRMWARE_VERSION ("firmwareVersion"),
        DEVICE ("device"),
        LAST_CHECKED("lastChecked"),
        INTERVAL("interval"),
        ;

        private String name;

        Fields(String name) {
            this.name = name;
        }

        public String fieldName(){
            return this.name;
        }
    }

    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareVersion> firmwareVersion = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private  Instant lastChecked;
    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Interval interval;

    @SuppressWarnings("unused")
    private long version;
    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;

    private final DataModel dataModel;
    private final EventService eventService;
    private final FirmwareService firmwareService;

    @Inject
    public ActivatedFirmwareVersionImpl(DataModel dataModel, EventService eventService, FirmwareService firmwareService) {
        this.dataModel = dataModel;
        this.eventService = eventService;
        this.firmwareService = firmwareService;
    }

    public ActivatedFirmwareVersion init(Device device, FirmwareVersion firmwareVersion, Interval interval) {
        this.device.set(device);
        this.firmwareVersion.set(firmwareVersion);
        this.interval = interval;
        return this;
    }

    public static ActivatedFirmwareVersion from(DataModel dataModel, Device device, FirmwareVersion firmwareVersion, Interval interval) {
        return dataModel.getInstance(ActivatedFirmwareVersionImpl.class).init(device, firmwareVersion, interval);
    }

    @Override
    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.validate(dataModel, this);
        getActivatedFirmwareVersionWithTheSameType().ifPresent(activeFirmwareVersion -> {
            ((ActivatedFirmwareVersionImpl) activeFirmwareVersion).expiredAt(this.getInterval().getStart());
        });
        dataModel.persist(this);
        notifyCreated();
    }

    private Optional<ActivatedFirmwareVersion> getActivatedFirmwareVersionWithTheSameType() {
        FirmwareType firmwareType = this.getFirmwareVersion().getFirmwareType();
        return firmwareService.getActiveFirmwareVersion(this.getDevice(), firmwareType);
    }

    private void expiredAt(Instant end){
        if (this.isEffectiveAt(end)) {
            this.interval = this.interval.withEnd(end);
            dataModel.update(this);
        }
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
        notifyUpdated();
    }

    private void notifyCreated() {
        this.eventService.postEvent(EventType.ACTIVATED_FIRMWARE_VERSION_CREATED.topic(), this);
    }

    private void notifyUpdated() {
        this.eventService.postEvent(EventType.ACTIVATED_FIRMWARE_VERSION_UPDATED.topic(), this);
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Device getDevice() {
        return device.orNull();
    }

    public void setDevice(Device device) {
        this.device.set(device);
    }

    @Override
    public FirmwareVersion getFirmwareVersion() {
        return firmwareVersion.orNull();
    }

    @Override
    public Instant getLastChecked() {
        return this.lastChecked;
    }

    @Override
    public void setLastChecked(Instant lastChecked) {
        this.lastChecked = lastChecked;
    }

    public void setFirmwareVersion(FirmwareVersion firmwareVersion) {
        this.firmwareVersion.set(firmwareVersion);
    }

    @Override
    public Interval getInterval() {
        return this.interval;
    }
}
