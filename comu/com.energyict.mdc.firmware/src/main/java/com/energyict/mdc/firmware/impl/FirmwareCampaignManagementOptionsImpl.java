/*
 * Copyright (c) 2019 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.firmware.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.orm.DataModel;
import com.elster.jupiter.orm.associations.IsPresent;
import com.elster.jupiter.orm.associations.Reference;
import com.elster.jupiter.orm.associations.ValueReference;

import com.energyict.mdc.firmware.FirmwareCampaign;
import com.energyict.mdc.firmware.FirmwareCampaignManagementOptions;
import com.energyict.mdc.firmware.FirmwareCheckManagementOption;
import com.energyict.mdc.firmware.FirmwareStatus;

import javax.inject.Inject;
import java.util.EnumSet;
import java.util.Set;

public class FirmwareCampaignManagementOptionsImpl implements FirmwareCampaignManagementOptions {

    enum Fields {
        FWRCAMPAIGN("firmwareCampaign"),
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
    private final Reference<FirmwareCampaign> firmwareCampaign = ValueReference.absent();

    private boolean checkFinalTargetFirmwareStatus;
    private boolean checkTestTargetFirmwareStatus;
    private boolean checkCurrentFirmware;
    private boolean checkMasterFirmwareWithFinalStatus;
    private boolean checkMasterFirmwareWithTestStatus;

    private final DataModel dataModel;

    @Inject
    public FirmwareCampaignManagementOptionsImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    public FirmwareCampaignManagementOptionsImpl init(FirmwareCampaign firmwareCampaign) {
        this.firmwareCampaign.set(firmwareCampaign);
        return this;
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
        if (dataModel.mapper(FirmwareCampaignManagementOptions.class).getUnique("firmwareCampaign", firmwareCampaign.get()).isPresent()) {
            Save.UPDATE.save(dataModel, this);
        } else {
            Save.CREATE.save(dataModel, this);
        }
    }

}
