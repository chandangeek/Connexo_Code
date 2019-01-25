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
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareManagementOptions;
import com.energyict.mdc.firmware.FirmwareStatus;
import com.energyict.mdc.upl.messages.ProtocolSupportedFirmwareOptions;

import javax.inject.Inject;
import java.time.Instant;
import java.util.EnumSet;
import java.util.Set;

@FirmwareManagementOptionHasAtLeastOneOption(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
public class FirmwareManagementOptionsImpl implements FirmwareManagementOptions {

    enum Fields {
        DEVICETYPE("deviceType"),
        INSTALL("install"),
        ACTIVATE("activate"),
        ACTIVATEONDATE("activateOnDate"),
        CHK_CURRENT_FW_FOR_FINAL("checkCurrentFirmwareForFinalFirmwareUpload"),
        CHK_CURRENT_FW_FOR_TEST("checkCurrentFirmwareForTestFirmwareUpload"),
        CHK_MASTER_FW_FOR_FINAL("checkMasterFirmwareForFinalFirmwareUpload"),
        CHK_MASTER_FW_FOR_TEST("checkMasterFirmwareForTestFirmwareUpload");

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

    private boolean checkCurrentFirmwareForFinalFirmwareUpload;
    private boolean checkCurrentFirmwareForTestFirmwareUpload;
    private boolean checkMasterFirmwareForFinalFirmwareUpload;
    private boolean checkMasterFirmwareForTestFirmwareUpload;

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
        Set<ProtocolSupportedFirmwareOptions> allowedOptions = EnumSet.noneOf(ProtocolSupportedFirmwareOptions.class);
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
    public boolean isActivated(FirmwareCheckManagementOption checkManagementOption) {
        switch (checkManagementOption) {
            case CURRENT_FIRMWARE_CHECK:
                return checkCurrentFirmwareForFinalFirmwareUpload || checkCurrentFirmwareForTestFirmwareUpload;
            case MASTER_FIRMWARE_CHECK:
                return checkMasterFirmwareForFinalFirmwareUpload || checkMasterFirmwareForTestFirmwareUpload;
            default:
                throw new IllegalArgumentException("Unknown firmware check management option!");
        }
    }

    @Override
    public EnumSet<FirmwareStatus> getTargetFirmwareStatuses(FirmwareCheckManagementOption checkManagementOption) {
        EnumSet<FirmwareStatus> statuses = EnumSet.noneOf(FirmwareStatus.class);
        switch (checkManagementOption) {
            case CURRENT_FIRMWARE_CHECK:
                if (checkCurrentFirmwareForFinalFirmwareUpload) {
                    statuses.add(FirmwareStatus.FINAL);
                }
                if (checkCurrentFirmwareForTestFirmwareUpload) {
                    statuses.add(FirmwareStatus.TEST);
                }
                break;
            case MASTER_FIRMWARE_CHECK:
                if (checkMasterFirmwareForFinalFirmwareUpload) {
                    statuses.add(FirmwareStatus.FINAL);
                }
                if (checkMasterFirmwareForTestFirmwareUpload) {
                    statuses.add(FirmwareStatus.TEST);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown firmware check management option!");
        }
        return statuses;
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
        allowedOptions.forEach(op -> {
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
    public void activateFirmwareCheck(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses) {
        switch (checkManagementOption) {
            case CURRENT_FIRMWARE_CHECK:
                checkCurrentFirmwareForFinalFirmwareUpload = firmwareStatuses.contains(FirmwareStatus.FINAL);
                checkCurrentFirmwareForTestFirmwareUpload = firmwareStatuses.contains(FirmwareStatus.TEST);
                break;
            case MASTER_FIRMWARE_CHECK:
                checkMasterFirmwareForFinalFirmwareUpload = firmwareStatuses.contains(FirmwareStatus.FINAL);
                checkMasterFirmwareForTestFirmwareUpload = firmwareStatuses.contains(FirmwareStatus.TEST);
                break;
            default:
                throw new IllegalArgumentException("Unknown firmware check management option!");
        }
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
