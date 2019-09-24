/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.tasks.FirmwareManagementTask;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.upl.messages.DeviceMessageSpec;
import com.energyict.mdc.upl.offline.DeviceOfflineFlags;

import javax.inject.Inject;

/**
 * Straightforward implementation of the FirmwareUpgradeTask
 */
public class FirmwareManagementTaskImpl extends ProtocolTaskImpl implements FirmwareManagementTask {

    private DeviceMessageSpecificationService deviceMessageSpecificationService;

    public FirmwareManagementTaskImpl() {
        super();
        setFlags(new DeviceOfflineFlags(DeviceOfflineFlags.PENDING_MESSAGES_FLAG, DeviceOfflineFlags.SENT_MESSAGES_FLAG));
    }

    @Inject
    FirmwareManagementTaskImpl(DataModel dataModel, DeviceMessageSpecificationService deviceMessageSpecificationService) {
        super(dataModel);
        this.deviceMessageSpecificationService = deviceMessageSpecificationService;
        setFlags(new DeviceOfflineFlags(DeviceOfflineFlags.PENDING_MESSAGES_FLAG, DeviceOfflineFlags.SENT_MESSAGES_FLAG));
    }

    @Override
    public boolean isFirmwareUpgradeTask() {
        return true;
    }

    @Override
    void deleteDependents() {
        // currently no dependents to delete
    }

    @Override
    public boolean isValidFirmwareCommand(DeviceMessageSpec deviceMessageSpec) {
        return this.deviceMessageSpecificationService.getFirmwareCategory().getId() == deviceMessageSpec.getCategory().getId();
    }


}
