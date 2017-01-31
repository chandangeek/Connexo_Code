/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.device.config.DeviceType;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.protocol.api.firmware.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@FirmwareManagementOptionHasAtLeastOneOption(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class FirmwareManagementOptionsImpl implements FirmwareManagementOptions {

    enum Fields {
        DEVICETYPE("deviceType"),
        INSTALL("install"),
        ACTIVATE("activate"),
        ACTIVATEONDATE("activateOnDate");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }


    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private Reference<DeviceType> deviceType = ValueReference.absent();
    private boolean install;
    private boolean activate;
    private boolean activateOnDate;

    @SuppressWarnings("unused")
    private Instant createTime;
    @SuppressWarnings("unused")
    private Instant modTime;
    @SuppressWarnings("unused")
    private String userName;
    @SuppressWarnings("unused")
    private long version;

    private final DataModel dataModel;
    
    @Inject
    public FirmwareManagementOptionsImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FirmwareManagementOptions init(DeviceType deviceType) {
        this.deviceType.set(deviceType);
        return this;
    }

    @Override
    public Set<ProtocolSupportedFirmwareOptions> getOptions() {
        Set<ProtocolSupportedFirmwareOptions> allowedOptions = new LinkedHashSet<>();
        if (install) {
            allowedOptions.add(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_LATER);
        }
        if (activate) {
            allowedOptions.add(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_IMMEDIATE);
        }
        if (activateOnDate) {
            allowedOptions.add(ProtocolSupportedFirmwareOptions.UPLOAD_FIRMWARE_AND_ACTIVATE_WITH_DATE);
        }
        return allowedOptions;
    }

    @Override
    public void delete() {
        dataModel.remove(this);
    }

    @Override
    public long getVersion() {
        return this.version;
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

    @Override
    public void save() {
        if (dataModel.mapper(FirmwareManagementOptions.class).getUnique("deviceType", deviceType.get()).isPresent()) {
            doUpdate();
        } else {
            doPersist();
        }
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

    DeviceType getDeviceType(){
        return this.deviceType.get();
    }
}
