package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.elster.jupiter.util.time.Interval;
import com.energyict.mdc.device.data.Device;
import com.energyict.mdc.firmware.ActivatedFirmwareVersion;
import com.energyict.mdc.firmware.FirmwareVersion;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.time.Instant;

public class ActivatedFirmwareVersionImpl implements ActivatedFirmwareVersion {

    private long id;
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<FirmwareVersion> firmwareVersion = ValueReference.absent();
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<Device> device = ValueReference.absent();
    private  Instant lastChecked;

    @NotNull(message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Interval interval;

    private long version;
    private Instant createTime;
    private Instant modTime;
    private String userName;

    private final DataModel dataModel;

    @Inject
    public ActivatedFirmwareVersionImpl(DataModel dataModel) {
        this.dataModel = dataModel;
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

    public void save() {
        if (getId() == 0) {
            doPersist();
            return;
        }
        doUpdate();
    }

    private void doPersist() {
        Save.CREATE.save(dataModel, this);
    }

    private void doUpdate() {
        Save.UPDATE.save(dataModel, this);
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
