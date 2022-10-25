/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;
import com.energyict.mdc.common.device.config.DeviceType;
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
        CHK_TARGET_FW_FINAL("checkFinalTargetFirmwareStatus"),
        CHK_TARGET_FW_TEST("checkTestTargetFirmwareStatus"),
        CHK_CURRENT_FW("checkCurrentFirmware"),
        CHK_MASTER_FW_FINAL("checkMasterFirmwareWithFinalStatus"),
        CHK_MASTER_FW_TEST("checkMasterFirmwareWithTestStatus");

        private final String javaFieldName;

        Fields(String javaFieldName) {
            this.javaFieldName = javaFieldName;
        }

        String fieldName() {
            return javaFieldName;
        }
    }

    @IsPresent(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.FIELD_IS_REQUIRED + "}")
    private final Reference<DeviceType> deviceType = ValueReference.absent();
    private boolean install;
    private boolean activate;
    private boolean activateOnDate;

    private boolean checkFinalTargetFirmwareStatus;
    private boolean checkTestTargetFirmwareStatus;
    private boolean checkCurrentFirmware;
    private boolean checkMasterFirmwareWithFinalStatus;
    private boolean checkMasterFirmwareWithTestStatus;

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
            case TARGET_FIRMWARE_STATUS_CHECK:
                return checkFinalTargetFirmwareStatus || checkTestTargetFirmwareStatus;
            case CURRENT_FIRMWARE_CHECK:
                return checkCurrentFirmware;
            case MASTER_FIRMWARE_CHECK:
                return checkMasterFirmwareWithFinalStatus || checkMasterFirmwareWithTestStatus;
            default:
                throw new IllegalArgumentException("Unknown firmware check management option!");
        }
    }

    @Override
    public EnumSet<FirmwareStatus> getStatuses(FirmwareCheckManagementOption checkManagementOption) {
        EnumSet<FirmwareStatus> statuses = EnumSet.noneOf(FirmwareStatus.class);
        switch (checkManagementOption) {
            case TARGET_FIRMWARE_STATUS_CHECK:
                if (checkFinalTargetFirmwareStatus) {
                    statuses.add(FirmwareStatus.FINAL);
                }
                if (checkTestTargetFirmwareStatus) {
                    statuses.add(FirmwareStatus.TEST);
                }
                break;
            case CURRENT_FIRMWARE_CHECK:
                return null; // no status set is applicable for this check
            case MASTER_FIRMWARE_CHECK:
                if (checkMasterFirmwareWithFinalStatus) {
                    statuses.add(FirmwareStatus.FINAL);
                }
                if (checkMasterFirmwareWithTestStatus) {
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
    public void activateFirmwareCheckWithStatuses(FirmwareCheckManagementOption checkManagementOption, Set<FirmwareStatus> firmwareStatuses) {
        switch (checkManagementOption) {
            case TARGET_FIRMWARE_STATUS_CHECK:
                checkFinalTargetFirmwareStatus = firmwareStatuses.contains(FirmwareStatus.FINAL);
                checkTestTargetFirmwareStatus = firmwareStatuses.contains(FirmwareStatus.TEST);
                break;
            case CURRENT_FIRMWARE_CHECK:
                checkCurrentFirmware = true;
                break;
            case MASTER_FIRMWARE_CHECK:
                checkMasterFirmwareWithFinalStatus = firmwareStatuses.contains(FirmwareStatus.FINAL);
                checkMasterFirmwareWithTestStatus = firmwareStatuses.contains(FirmwareStatus.TEST);
                break;
            default:
                throw new IllegalArgumentException("Unknown firmware check management option!");
        }
    }

    @Override
    public void deactivate(FirmwareCheckManagementOption checkManagementOption) {
        switch (checkManagementOption) {
            case TARGET_FIRMWARE_STATUS_CHECK:
                checkFinalTargetFirmwareStatus = false;
                checkTestTargetFirmwareStatus = false;
                break;
            case CURRENT_FIRMWARE_CHECK:
                checkCurrentFirmware = false;
                break;
            case MASTER_FIRMWARE_CHECK:
                checkMasterFirmwareWithFinalStatus = false;
                checkMasterFirmwareWithTestStatus = false;
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

    DeviceType getDeviceType() {
        return this.deviceType.get();
    }
}
