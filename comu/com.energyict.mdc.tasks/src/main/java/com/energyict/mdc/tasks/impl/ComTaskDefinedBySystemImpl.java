/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.tasks.impl;

import com.elster.jupiter.domain.util.Save;
import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.tasks.FirmwareManagementTask;

import com.google.inject.Provider;

import javax.inject.Inject;

/**
 * An implementation of a System defined ComTask
 */
// TODO: uncomment Save.Update.class.
// TODO: Was commented out only due to the bug blocking all firmware upgrades (not possible to add Firmware management com task to device configuration)
@UniqueComTaskForFirmwareUpgrade(groups = {Save.Create.class/*, Save.Update.class*/}, message = "{" + MessageSeeds.Keys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED + "}")
@OnlyOneProtocolTaskIfFirmwareUpgrade(groups = {Save.Create.class, Save.Update.class}, message = "{" + MessageSeeds.Keys.ONLY_ONE_COMTASK_WITH_FIRMWARE_ALLOWED + "}")
class ComTaskDefinedBySystemImpl extends ComTaskImpl implements SystemComTask {

    @Inject
    ComTaskDefinedBySystemImpl(DataModel dataModel, Thesaurus thesaurus,
                                    EventService eventService,
                                    Provider<BasicCheckTaskImpl> basicCheckTaskProvider,
                                    Provider<ClockTaskImpl> clockTaskProvider,
                                    Provider<LoadProfilesTaskImpl> loadProfilesTaskProvider,
                                    Provider<LogBooksTaskImpl> logBooksTaskProvider,
                                    Provider<MessagesTaskImpl> messagesTaskProvider,
                                    Provider<RegistersTaskImpl> registersTaskProvider,
                                    Provider<StatusInformationTaskImpl> statusInformationTaskProvider,
                                    Provider<TopologyTaskImpl> topologyTaskProvider,
                                    Provider<FirmwareManagementTaskImpl> firmwareManagementTaskProvider) {
        super(logBooksTaskProvider, dataModel, statusInformationTaskProvider, messagesTaskProvider, basicCheckTaskProvider, registersTaskProvider, eventService, clockTaskProvider, topologyTaskProvider, thesaurus, loadProfilesTaskProvider, firmwareManagementTaskProvider);
    }

    @Override
    public FirmwareManagementTask createFirmwareUpgradeTask() {
        FirmwareManagementTaskImpl firmwareUpgradeTask = this.getFirmwareManagementTaskProvider().get();
        firmwareUpgradeTask.ownedBy(this);
        addProtocolTask(firmwareUpgradeTask);
        return firmwareUpgradeTask;
    }

    @Override
    public boolean isUserComTask() {
        return false;
    }

    @Override
    public boolean isSystemComTask() {
        return true;
    }

}
