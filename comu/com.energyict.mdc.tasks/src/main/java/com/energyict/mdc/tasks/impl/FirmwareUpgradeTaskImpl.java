package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpec;
import com.energyict.mdc.protocol.api.device.messages.DeviceMessageSpecificationService;
import com.energyict.mdc.protocol.api.device.offline.DeviceOfflineFlags;
import com.energyict.mdc.tasks.FirmwareUpgradeTask;

import javax.inject.Inject;

/**
 * Straightforward implementation of the FirmwareUpgradeTask
 */
public class FirmwareUpgradeTaskImpl extends ProtocolTaskImpl implements FirmwareUpgradeTask {

    private final DeviceMessageSpecificationService deviceMessageSpecificationService;

    @Inject
    FirmwareUpgradeTaskImpl(DataModel dataModel, DeviceMessageSpecificationService deviceMessageSpecificationService) {
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
