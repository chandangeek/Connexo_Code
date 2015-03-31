package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareService;
import com.energyict.mdc.firmware.FirmwareUpgradeOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

public class FirmwareUpgradeOptionsImpl implements FirmwareUpgradeOptions {
    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private boolean install;
    private boolean activate;
    private boolean activateOnDate;

    @SuppressWarnings("unused")
    private Instant createTime;
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    private long version;

    private final DataModel dataModel;
    private final FirmwareService firmwareService;

    @Inject
    public FirmwareUpgradeOptionsImpl(DataModel dataModel, FirmwareService firmwareService) {
        this.dataModel = dataModel;
        this.firmwareService = firmwareService;
    }

    private FirmwareUpgradeOptions init(DeviceType deviceType) {
        this.deviceType.set(deviceType);
        return this;
    }

    public static FirmwareUpgradeOptions from(DataModel dataModel, DeviceType deviceType) {
        return dataModel.getInstance(FirmwareUpgradeOptionsImpl.class).init(deviceType);
    }

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getOptions() {
        Set<ProtocolSupportedFirmwareOptions> allowedOptions = new LinkedHashSet<>();
        if (install) {
            allowedOptions.add(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
        if(activate) {
            allowedOptions.add(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
        if (activateOnDate) {
            allowedOptions.add(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
        return allowedOptions;
    }

    @Override
    public void setOptions(Set<ProtocolSupportedFirmwareOptions> allowedOptions) {
        clearOptions();
        allowedOptions.stream().forEach(op -> {
            switch (op) {
                case UPLOAD_FIRMWARE_AND_ACTIVATE_LATER:
                    this.install = true;
                    break;
                case UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE:
                    this.activate = true;
                    break;
                case UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE:
                    this.activateOnDate = true;
            }
        });
    }

    public void save() {
        if (firmwareService.getAllowedFirmwareUpgradeOptionsFor(this.deviceType.get()).isEmpty()) {
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

    private void clearOptions() {
        this.install = false;
        this.activate = false;
        this.activateOnDate = false;
    }

}
