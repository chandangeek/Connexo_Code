package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.tasks.FirmwareUpgradeTask;

import javax.inject.Inject;

/**
 * Straightforward implementation of the FirmwareUpgradeTask
 */
public class FirmwareUpgradeTaskImpl extends ProtocolTaskImpl implements FirmwareUpgradeTask {

    @Inject
    FirmwareUpgradeTaskImpl(DataModel dataModel) {
        super(dataModel);
    }

    @Override
    public boolean isFirmwareUpgradeTask() {
        return true;
    }

    @Override
    void deleteDependents() {
        // currently no dependents to delete
    }
}
